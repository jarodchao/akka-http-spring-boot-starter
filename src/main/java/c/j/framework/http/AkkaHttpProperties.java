package c.j.framework.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.List;

@ConfigurationProperties(prefix = "spring.akka.http")
public class AkkaHttpProperties {

    private String applicationName;

    private String bindHost = "0.0.0.0";

    private int port = 8080;

    private String contextPath = "";

    private String requestClass;

    private String responseClass;

    private List<Route> routes;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getBindHost() {
        return bindHost;
    }

    public void setBindHost(String bindHost) {
        this.bindHost = bindHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        if (contextPath.startsWith("/"))
            this.contextPath = contextPath.replaceFirst("/", "");
        else
            this.contextPath = contextPath;
    }

    public String getRequestClass() {
        return requestClass;
    }

    public void setRequestClass(String requestClass) {
        this.requestClass = requestClass;
    }

    public String getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(String responseClass) {
        this.responseClass = responseClass;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public static class Route {

        private String url;

        private String httpMethod = "POST";

        private String grooveId;

        private String method;

        private String requestClass;

        private String responseClass;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            if (url.startsWith("/"))
                this.url = url.replaceFirst("/", "");
            else
                this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getGrooveId() {
            return grooveId;
        }

        public void setGrooveId(String grooveId) {
            this.grooveId = grooveId;
        }

        public String getRequestClass() {
            return requestClass;
        }

        public void setRequestClass(String requestClass) {
            this.requestClass = requestClass;
        }

        public String getResponseClass() {
            return responseClass;
        }

        public void setResponseClass(String responseClass) {
            this.responseClass = responseClass;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod.toUpperCase();
        }

        public String setFullUrl(String contextPath) {

            this.url = StringUtils.isEmpty(contextPath) ? this.url : contextPath + "/" + this.url;

            return this.url;
        }

        public String getSinkId() {
            return httpMethod + ":" + url;
        }

        public Class requestClass(){
            try {
                return Class.forName(this.requestClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

        public Route(String url, String httpMethod, String grooveId, String method, String requestClass, String responseClass) {
            this.url = url;
            this.httpMethod = httpMethod;
            this.grooveId = grooveId;
            this.method = method;
            this.requestClass = requestClass;
            this.responseClass = responseClass;
        }

        public Route() {
        }
    }

}