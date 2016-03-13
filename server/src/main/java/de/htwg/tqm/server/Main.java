package de.htwg.tqm.server;

import de.htwg.tqm.server.push.WebSocketPushService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import java.io.IOException;
import java.util.Collections;

@SuppressWarnings("ResultOfMethodCallIgnored")
final class Main {

    public static void main(String[] args) throws IOException {
        // WebSocketPushService must be created manually as it must be registered BEFORE the HTTP server is started.
        // (That means the HK2 service locator is not yet available).
        // The object is bound as singleton in the TqmBinder.
        // Its fields will be injected on container startup.
        WebSocketPushService pushService = new WebSocketPushService();
        AbstractBinder binder = new TqmBinder(pushService);
        ContainerLifecycleListener lifecycleListener = new TqmLifecycleListener(Collections.singletonList(pushService));
        ResourceConfig resourceConfig = new TqmResourceConfig(binder, lifecycleListener);
        TqmHttpServerBuilder serverBuilder = new TqmHttpServerBuilder(pushService, resourceConfig);
        HttpServer httpServer = serverBuilder.build();

        httpServer.start();

        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", TqmHttpServerBuilder.getBaseUri()));

        System.in.read();
        httpServer.shutdown();
    }
}
