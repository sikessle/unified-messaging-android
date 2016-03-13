package de.htwg.tqm.server;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TqmResourceConfigTest {

    @Test
    public void testRegistration() throws Exception {
        AbstractBinder mockBinder = mock(AbstractBinder.class);
        ContainerLifecycleListener mockListener = mock(ContainerLifecycleListener.class);
        TqmResourceConfig sut = new TqmResourceConfig(mockBinder, mockListener);

        assertThat(sut.isRegistered(JacksonFeature.class), is(true));
        assertThat(sut.isRegistered(mockBinder), is(true));
        assertThat(sut.isRegistered(mockListener), is(true));
    }
}