package de.htwg.tqm.server;

import de.htwg.tqm.server.push.WebSocketPushService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

public abstract class ServerTest extends JerseyTest {

    private TqmHttpServerBuilder serverBuilder;

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return (uri, deploymentContext) -> new TqmTestContainer();
    }

    @Override
    protected Application configure() {
        AbstractBinder binder = getBinder();
        WebSocketPushService pushService = new WebSocketPushService();

        if (binder == null) {
            binder = new TqmBinder(pushService);
        }

        ContainerLifecycleListener lifecycleListener = new TqmLifecycleListener(Collections.singletonList(pushService));
        ResourceConfig resourceConfig = new TqmResourceConfig(binder, lifecycleListener);

        serverBuilder = new TqmHttpServerBuilder(pushService, resourceConfig);

        return resourceConfig;
    }

    protected abstract @Nullable AbstractBinder getBinder();

    private class TqmTestContainer implements TestContainer {

        private HttpServer server;

        @Override
        public ClientConfig getClientConfig() {
            return null;
        }

        @Override
        public URI getBaseUri() {
            return URI.create(TqmHttpServerBuilder.getBaseUri());
        }

        @Override
        public void start() {
            server = serverBuilder.build();
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void stop() {
            server.shutdown();
        }

    }
}
