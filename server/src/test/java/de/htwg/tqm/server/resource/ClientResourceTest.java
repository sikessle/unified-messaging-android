package de.htwg.tqm.server.resource;

import de.htwg.tqm.server.ServerTest;
import de.htwg.tqm.server.TqmBinder;
import de.htwg.tqm.server.beans.ClientBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.push.PushService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class ClientResourceTest extends ServerTest {

    private final ClientService mockRegistry = mock(ClientService.class);
    private final static String USER = "user";
    private final static String PROJECT = "projectKey";
    private final static Client.Role ROLE = Client.Role.DEV;
    private final Client client = new ClientBean(USER, PROJECT, ROLE);

    @Override
    protected @Nullable AbstractBinder getBinder() {
        return new TestBinder(mock(PushService.class));
    }

    @Test
    public void testRegisterClient() {
        final ClientCreateRequestBean registryMessage = new ClientCreateRequestBean(client.getName(), client.getProject(), client.getRole().toString());

        final Response response = target("/tqm/rest/clients")
                .request()
                .post(Entity.entity(registryMessage, MediaType.APPLICATION_JSON));


        verify(mockRegistry).registerClient(eq(client));
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void testRegisterWrongRole() throws Exception {
        final ClientCreateRequestBean registryMessage = new ClientCreateRequestBean(client.getName(), client.getProject(), "WRONG ROLE");

        final Response response = target("/tqm/rest/clients")
                .request()
                .post(Entity.entity(registryMessage, MediaType.APPLICATION_JSON));

        verify(mockRegistry, never()).registerClient(any());
        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void testUnregisterClient() {
        when(mockRegistry.getClient(client.getName())).thenReturn(client);
        final Response response = target("/tqm/rest/clients/" + client.getName()).request().delete();

        verify(mockRegistry).unregisterClient(eq(client.getName()));
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void testUnregisterMissingUsername() {
        final Response response = target("/tqm/rest/clients/").request().delete();

        assertThat(response.getStatus(), is(Response.Status.METHOD_NOT_ALLOWED.getStatusCode()));
    }

    private class TestBinder extends TqmBinder {

        public TestBinder(@NotNull PushService pushService) {
            super(pushService);
        }

        @Override
        protected void configure() {
            // Will have precedence
            bind(mockRegistry).to(ClientService.class);
            super.configure();
        }
    }

}
