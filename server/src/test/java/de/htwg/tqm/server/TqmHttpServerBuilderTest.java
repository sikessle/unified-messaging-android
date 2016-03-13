package de.htwg.tqm.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TqmHttpServerBuilderTest {

    private TqmHttpServerBuilder sut;

    @Before
    public void setUp() throws Exception {
        WebSocketApplication webSocketAppMock = mock(WebSocketApplication.class);
        ResourceConfig resourceConfigMock = new ResourceConfig();
        sut = new TqmHttpServerBuilder(webSocketAppMock, resourceConfigMock);
    }

    @Test
    public void testBuild() throws Exception {
        assertThat(TqmHttpServerBuilder.getBaseUri().startsWith("http://"), is(true));
        assertThat(TqmHttpServerBuilder.getWebSocketUri().startsWith("ws://"), is(true));
        assertThat(TqmHttpServerBuilder.getWebSocketUri().endsWith("/tqm/ws"), is(true));

        HttpServer server = sut.build();

        assertThat(server.isStarted(), is(false));
    }
}