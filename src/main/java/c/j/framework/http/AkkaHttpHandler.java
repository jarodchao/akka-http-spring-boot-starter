package c.j.framework.http;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.IncomingConnection;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.ansi.AnsiPropertySource;
import org.springframework.core.env.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Slf4j
public class AkkaHttpHandler {

    private RouteHandler routeHandler;

    private int port;

    private String bindHost;

    private String applicationName;

    private ActorSystem system;

    private ActorMaterializer mat;

    public void setRouteHandler(RouteHandler routeHandler) {
        this.routeHandler = routeHandler;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setBindHost(String bindHost) {
        this.bindHost = bindHost;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setSystem(ActorSystem system) {
        this.system = system;
    }

    public void setMat(ActorMaterializer mat) {
        this.mat = mat;
    }

    private static final String BANNER_NAME = "classpath:banner-akka-http.txt";

    public void run() {

        final Http http = Http.get(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow
                = routeHandler.createRoute().flow(system, mat);

        Source<IncomingConnection, CompletionStage<ServerBinding>> serverSource =
                http.bind(ConnectHttp.toHost(bindHost, port), mat);

        CompletionStage<ServerBinding> serverBindingFuture =
                serverSource.to(Sink.foreach(connection -> connection.handleWith(routeFlow, mat))).run(mat);

        printServerInfo(log);

        serverBindingFuture.whenCompleteAsync((binding, failure) ->
                        log.error("Something very bad happened! " + failure.getMessage())
                , system.dispatcher());

    }

    private void printServerInfo(Logger logger) {
        ResourceLoader resourceLoader = new DefaultResourceLoader(ClassUtils.getDefaultClassLoader());
        Resource resource = resourceLoader.getResource(BANNER_NAME);

        try {
            String info = StreamUtils.copyToString(resource.getInputStream(),Charset.forName("UTF-8"));

            for (PropertyResolver resolver : getPropertyResolvers()) {
                info = resolver.resolvePlaceholders(info);
            }

            logger.info("\n" + info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<PropertyResolver> getPropertyResolvers() {
        List<PropertyResolver> resolvers = new ArrayList<>();
        resolvers.add(getInfoResolver());
        resolvers.add(getAnsiResolver());
        return resolvers;
    }

    private PropertyResolver getInfoResolver() {
        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources
                .addLast(new MapPropertySource("version", getInfosMap()));
        return new PropertySourcesPropertyResolver(propertySources);
    }

    private Map<String, Object> getInfosMap() {
        Map<String, Object> versions = new HashMap<String, Object>();
        String akkaVersion = system.settings().ConfigVersion();
        String akkaHttpVersion = getApplicationVersion(Http.class);
        versions.put("akka.version", getVersionString(akkaVersion,false));
        versions.put("akka.formatted-version", getVersionString(akkaVersion,true));
        versions.put("akka-http.version", getVersionString(akkaHttpVersion, false));
        versions.put("akka-http.formatted-version", getVersionString(akkaHttpVersion, true));
        versions.put("application.name", applicationName);
        versions.put("bind.host", bindHost);
        versions.put("bind.port", port);
        return versions;
    }

    private PropertyResolver getAnsiResolver() {
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(new AnsiPropertySource("ansi", true));
        return new PropertySourcesPropertyResolver(sources);
    }

    private String getVersionString(String version, boolean format) {
        if (version == null) {
            return "";
        }
        return (format ? " (v" + version + ")" : version);
    }

    protected String getApplicationVersion(Class<?> sourceClass) {
        Package sourcePackage = (sourceClass == null ? null : sourceClass.getPackage());
        return (sourcePackage == null ? null : sourcePackage.getImplementationVersion());
    }


}
