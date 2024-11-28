package tukano.impl.rest;

import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import utils.IP;

import utils.auth.*;

public class TukanoRestServer extends Application {

    final private static Logger Log =
        Logger.getLogger(TukanoRestServer.class.getName());

    static final String INETADDR_ANY = "0.0.0.0";
    static String SERVER_BASE_URI = "http://%s:%s/rest";

    public static final int PORT = 8080;

    public static String serverURI;
    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> resources = new HashSet<>();

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                           "%4$s: %5$s");
    }

    public TukanoRestServer() {
        serverURI = String.format(SERVER_BASE_URI, IP.hostname(), PORT);

        resources.add(RestBlobsResource.class); // could be a singleton? Kevin said so
        resources.add(RestUsersResource.class);
        resources.add(RestShortsResource.class);

        resources.add(RequestCookiesFilter.class);
        resources.add(ResponseCookiesFilter.class);
        resources.add(RequestCookiesCleanupFilter.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return resources;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    public static void main(String[] args) throws Exception {
        return;
    }
}
