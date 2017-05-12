package c.j.framework.http;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AkkaHttpProperties.class)
public class AkkHttpAutoConfiguration {

    @Autowired
    private AkkaHttpProperties properties;

    @Autowired
    private RoutesProcessor processor;

    @Bean
    public ActorSystem actorSystem(){
        ActorSystem system = ActorSystem.create(properties.getApplicationName());

        return system;
    }

    @Bean
    public ActorMaterializer actorMaterializer(ActorSystem system){
        ActorMaterializer mat = ActorMaterializer.create(system);
        return mat;
    }


    @Bean
    public RouteHandler routeHandler() throws ClassNotFoundException {

        Class In = Class.forName(properties.getRequestClass());
        Class Out = Class.forName(properties.getResponseClass());

        RouteHandler routeHandler = new RouteHandler();
        routeHandler.setIn(In);
        routeHandler.setOut(Out);
        routeHandler.setRoutesProcessor(processor);
        routeHandler.setContextPath(properties.getContextPath());
        routeHandler.setRoutes(properties.getRoutes());
        routeHandler.initRoute();

        return routeHandler;

    }

    @Bean
    public AkkaHttpHandler akkaHttpHandler(RouteHandler routeHandler, ActorSystem system, ActorMaterializer mat) {

        AkkaHttpHandler akkaHttpHandler = new AkkaHttpHandler();

        akkaHttpHandler.setApplicationName(properties.getApplicationName());
        akkaHttpHandler.setSystem(system);
        akkaHttpHandler.setMat(mat);
        akkaHttpHandler.setBindHost(properties.getBindHost());
        akkaHttpHandler.setPort(properties.getPort());
        akkaHttpHandler.setRouteHandler(routeHandler);
        akkaHttpHandler.run();
        return akkaHttpHandler;

    }


}
