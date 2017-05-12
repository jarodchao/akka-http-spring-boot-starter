package c.j.framework.http;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
public abstract class RoutesProcessor extends AllDirectives implements InitializingBean{

    private final Map<String, Object> groove = new HashMap<>();

    private final Map<String, RouteView> sink = new HashMap<>();

    @Autowired(required = false)
    private Authentication authentication;

    public abstract void assembleGroove();

    public abstract Route[] extensionRoutes();

    protected Optional authorize(Object in) {

        if (authentication != null)
            return authentication.authorize(in);
        else
            return Optional.empty();

    }

    public CompletionStage<?> runSink(String sinkID, Object in) {

        Optional auth = authorize(in);

        if (!auth.isPresent()) {
            RouteView routeView = sink.get(sinkID);

            return CompletableFuture.supplyAsync(() -> ReflectionUtils.invokeMethod(routeView.method, routeView.bean, JSON.parseObject(getData(in),
                    routeView.inClass)));
        } else
            return CompletableFuture.supplyAsync(() -> auth.get());

    }

    public CompletionStage<?> runSink(AkkaHttpProperties.Route route, Object inObj)  {

        RouteView routeView = sink.get(route.getSinkId());

        if (routeView == null) {
            Object bean = groove.get(route.getGrooveId());
            Class in = null;
            try {
                in = Class.forName(route.getRequestClass());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getCause());
            }
            Method method = ReflectionUtils.findMethod(bean.getClass(), route.getMethod(), in);

            routeView = new RouteView(route.getSinkId(), bean, method, in);

            sink.put(route.getSinkId(), routeView);
        }
        Method method = routeView.method;
        Object bean = routeView.bean;
        Class inputClass = routeView.inClass;


        return CompletableFuture.supplyAsync(() -> ReflectionUtils.invokeMethod(method, bean, JSON.parseObject(getData(inObj),inputClass)));
    }


    public void pull(String id, Object bean) {
        groove.put(id, bean);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assembleGroove();
    }

    protected void assembleRouteView(List<AkkaHttpProperties.Route> routes) throws ClassNotFoundException {
        for (AkkaHttpProperties.Route route : routes) {
            log.info("Route:{} Method:{} GrooveId:{} Request:{} Response:{}",route.getUrl(),route.getMethod(),route.getGrooveId(),route
            .getRequestClass(),route.getResponseClass());
            System.out.println(String.format("Route:{%s} Method:{%s} GrooveId:{%s} Request:{%s} Response:{%s}",route.getUrl(),route.getMethod(),route.getGrooveId(),route
                    .getRequestClass(),route.getResponseClass()));
            Object bean = groove.get(route.getGrooveId());
            Class in = Class.forName(route.getRequestClass());
            Method method = ReflectionUtils.findMethod(bean.getClass(), route.getMethod(), in);

            sink.put(route.getSinkId(), new RouteView(route.getSinkId(), bean, method, in));
        }
    }

    public class RouteView {

        String sinkId;

        Object bean;

        Method method;

        Class<?> inClass;

        public RouteView(String sinkId, Object bean, Method method, Class<?> inClass) {
            this.sinkId = sinkId;
            this.bean = bean;
            this.method = method;
            this.inClass = inClass;
        }

    }
    public abstract <T> String getData(T t);
}
