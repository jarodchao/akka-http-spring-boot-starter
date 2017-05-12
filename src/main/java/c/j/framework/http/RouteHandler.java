package c.j.framework.http;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.lang.Class.forName;

@Slf4j
public class RouteHandler extends AllDirectives {

    private RoutesProcessor routesProcessor;

    private Class<?> in;

    private Class<?> out;

    private String contextPath;

    private List<AkkaHttpProperties.Route> routes;

    public void setIn(Class<?> in) {
        this.in = in;
    }

    public void setOut(Class<?> out) {
        this.out = out;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void setRoutesProcessor(RoutesProcessor routesProcessor) {
        this.routesProcessor = routesProcessor;
    }

    public void setRoutes(List<AkkaHttpProperties.Route> routes) {
        this.routes = routes;

        for (AkkaHttpProperties.Route route : this.routes) {
            if (StringUtils.isEmpty(route.getRequestClass()))
                route.setRequestClass(in.getCanonicalName());

            if (StringUtils.isEmpty(route.getResponseClass()))
                route.setResponseClass(out.getCanonicalName());

            route.setFullUrl(contextPath);
        }
    }

    public void initRoute() {
        try {
            routesProcessor.assembleRouteView(this.routes);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.error("初始化route view 失败!");
        }

    }

    public Route createRoute() {

        Route[] defRoute = routes.stream().map(r ->
            post(() ->
                    path(r.getUrl() , () ->
                            entity(Jackson.unmarshaller(in), request -> {
                                CompletionStage<?> futureProcess
                                        = routesProcessor.runSink(r.getSinkId(), request);
                                return onSuccess(() -> futureProcess, res ->
                                        completeOK(res, Jackson.marshaller())
                                );
                            })))
        ).toArray(size -> new Route[size]);

        Route[] extension = routesProcessor.extensionRoutes();

        List<Route> allList = new LinkedList<>();
        allList.addAll(Arrays.asList(defRoute));

        if (extension != null)
            allList.addAll(Arrays.asList(extension));

        return route(allList.parallelStream().toArray(size -> new Route[size]));
    }
}
