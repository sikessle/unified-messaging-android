package de.htwg.tqm.server;

import de.htwg.tqm.server.poll.PollService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.spi.Container;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;


public class TqmLifecycleListenerTest {
    private TqmLifecycleListener sut;
    private Object injectedObject;
    private Container mockContainer;
    private ServiceLocator mockLocator;
    private PollService mockPollService;

    @Before
    public void setUp() throws Exception {
        injectedObject = new Object();
        Collection<?> objectsToInject = Collections.singletonList(injectedObject);
        sut = new TqmLifecycleListener(objectsToInject);

        mockContainer = mock(Container.class);
        mockLocator = mock(ServiceLocator.class);
        mockPollService = mock(PollService.class);

        when(mockLocator.getService(PollService.class)).thenReturn(mockPollService);
    }

    @Test
    public void testOnStartup() throws Exception {
        sut.startPollServiceAndInjectObjects(mockLocator);
        verify(mockPollService).start();

        verify(mockLocator).getService(PollService.class);
        verify(mockLocator).inject(injectedObject);
    }

    @Test
    public void testOnShutdown() throws Exception {
        // To let it initialize with service locator
        sut.startPollServiceAndInjectObjects(mockLocator);
        sut.onShutdown(mockContainer);

        verify(mockPollService).shutdown();
    }

}