package de.htwg.tqm.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

final class TqmHttpServerBuilder {
    private static final String BASE_URI = "http://localhost:8080/";
    private static final String WEBSOCKET_PATH = "/ws";
    private final WebSocketApplication webSocketApplication;
    private final ResourceConfig resourceConfig;

    public TqmHttpServerBuilder(@NotNull WebSocketApplication webSocketApplication,
                                @NotNull ResourceConfig resourceConfig) {
        this.webSocketApplication = webSocketApplication;
        this.resourceConfig = resourceConfig;
    }

    public HttpServer build() {
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), resourceConfig, false);
        // Must be done BEFORE the server is started
        registerWebSocketApplication(server);

        return server;
    }

    private void registerWebSocketApplication(HttpServer server) {
        final WebSocketAddOn addon = new WebSocketAddOn();

        server.getListeners().forEach(listener -> listener.registerAddOn(addon));

        WebSocketEngine.getEngine().register("/tqm", WEBSOCKET_PATH, webSocketApplication);
    }

    public static String getBaseUri() {
        return BASE_URI;
    }

    public static String getWebSocketUri() {
        return BASE_URI.replace("http", "ws") + "tqm" + WEBSOCKET_PATH;
    }
}

