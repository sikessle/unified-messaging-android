package de.htwg.tqm.server.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.htwg.tqm.server.ServerTest;
import de.htwg.tqm.server.TqmBinder;
import de.htwg.tqm.server.beans.ClientBean;
import de.htwg.tqm.server.beans.DialogBean;
import de.htwg.tqm.server.beans.DialogMessageBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.beans.Dialog;
import de.htwg.tqm.server.beans.DialogMessage;
import de.htwg.tqm.server.communication.DialogService;
import de.htwg.tqm.server.communication.DialogResponseWatcherService;
import de.htwg.tqm.server.beans.Notification;
import de.htwg.tqm.server.push.PushService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class DialogResourceTest extends ServerTest {

    private final ClientService mockClientService = mock(ClientService.class);
    private final DialogService mockDialogService = mock(DialogService.class);
    private final PushService mockPushService = mock(PushService.class);
    private final DialogResponseWatcherService mockResponseWatcher = mock(DialogResponseWatcherService.class);
    private final Client dev = new ClientBean("developer", "project", Client.Role.DEV);
    private final Client sm = new ClientBean("scrumMaster", "project", Client.Role.SM);
    private final static long DIALOG_ID = 10;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected @Nullable AbstractBinder getBinder() {
        return new TestBinder(mock(PushService.class));
    }

    @Before
    public void setUp() throws Exception {
        // For JerseyTest to work
        super.setUp();
        when(mockClientService.getClient(dev.getName())).thenReturn(dev);
        when(mockClientService.getClient(sm.getName())).thenReturn(sm);
        when(mockDialogService.createDialog(anyString(), anyLong(), any(), any())).thenReturn(DIALOG_ID);
    }

    @Test
    public void testCreateDialog() {
        final DialogCreateRequestBean createBean = new DialogCreateRequestBean("subject", 0, sm.getName(), dev.getName());
        final DialogCreatedResponseBean expectedResponse = new DialogCreatedResponseBean(DIALOG_ID);

        final Response response = target("/tqm/rest/dialogs").request().post(Entity.entity(createBean, MediaType.APPLICATION_JSON));
        final DialogCreatedResponseBean actualResponse = response.readEntity(DialogCreatedResponseBean.class);

        verify(mockClientService).getClient(eq(sm.getName()));
        verify(mockClientService).getClient(eq(dev.getName()));
        verify(mockDialogService).createDialog(eq(createBean.getSubject()), eq(0L), eq(sm), eq(dev));
        Assert.assertThat(actualResponse, equalTo(expectedResponse));
        assertThat(response.getStatus(), is(200));

        // Ensure a notification was sent to affected person
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(mockPushService).send(captor.capture());
        assertEquals(dev, captor.getValue().getReceiver());
        assertEquals(Notification.Type.dialogCreated, captor.getValue().getType());

        // Ensure the dialog is watched for responses
        verify(mockResponseWatcher).watchDialog(eq(DIALOG_ID));
    }

    @Test
    public void testCreateDialogMissingClient() throws Exception {
        final DialogCreateRequestBean createBean = new DialogCreateRequestBean("subject", 0, sm.getName(), dev.getName());

        // Clients are NOT registered before
        reset(mockClientService);
        final Response response = target("/tqm/rest/dialogs").request().post(Entity.entity(createBean, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testGetDialog() throws Exception {
        DialogMessage expectedMessage = new DialogMessageBean("user", 0, "body");
        Dialog expectedDialog = createDummyDialog(DIALOG_ID, expectedMessage);
        when(mockDialogService.getDialog(DIALOG_ID)).thenReturn(expectedDialog);

        final Response response = target("/tqm/rest/dialogs/" + DIALOG_ID).request().get();
        final Dialog actualDialog = response.readEntity(DialogBean.class);

        assertThat(actualDialog, equalTo(expectedDialog));
    }

    @Test
    public void testGetMissingDialog() throws Exception {
        when(mockDialogService.getDialog(DIALOG_ID)).thenReturn(null);
        final Response response = target("/tqm/rest/dialogs/" + DIALOG_ID).request().get();
        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testCreateMessageToDialog() throws Exception {
        DialogMessage expectedMessage = new DialogMessageBean(dev.getName(), 0, "body");
        Dialog expectedDialog = createDummyDialog(DIALOG_ID, expectedMessage);
        when(mockDialogService.getDialog(DIALOG_ID)).thenReturn(expectedDialog);
        DialogMessageCreateRequestBean createMessageRequestBean = new DialogMessageCreateRequestBean(expectedMessage.getAuthor(), expectedMessage.getBody());

        target("/tqm/rest/dialogs/" + DIALOG_ID).request().post(Entity.entity(createMessageRequestBean, MediaType.APPLICATION_JSON));

        ArgumentCaptor<DialogMessage> messageCaptor = ArgumentCaptor.forClass(DialogMessage.class);
        verify(mockDialogService).addMessage(eq(DIALOG_ID), messageCaptor.capture());
        final DialogMessage actualMessage = messageCaptor.getValue();
        assertThat(actualMessage.getAuthor(), equalTo(expectedMessage.getAuthor()));
        assertThat(actualMessage.getBody(), equalTo(expectedMessage.getBody()));
        assertEquals(Instant.now().toEpochMilli(), actualMessage.getTimestamp(), 5 * 1000);

        // Ensure the other dialog person is notified about the new message to him
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(mockPushService).send(notificationCaptor.capture());
        assertEquals(sm, notificationCaptor.getValue().getReceiver());
        assertEquals(Notification.Type.dialogMessageCreated, notificationCaptor.getValue().getType());
    }

    @Test
    public void testCreateMessageToMissingDialog() throws Exception {
        when(mockDialogService.getDialog(DIALOG_ID)).thenReturn(null);
        DialogMessageCreateRequestBean createMessageRequestBean = new DialogMessageCreateRequestBean("user", "body");
        Response response = target("/tqm/rest/dialogs/" + DIALOG_ID).request()
                .post(Entity.entity(createMessageRequestBean, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    private Dialog createDummyDialog(long dialogID, DialogMessage message) {
        return new DialogBean(dialogID, "subject", 2, new TreeSet<>(Collections.singleton(message)),
                sm.getName(), dev.getName(), false, false, 10L);
    }

    @Test
    public void testGetDialogsForUsernameNotFound() throws Exception {
        Response response = target("/tqm/rest/dialogs/users/aNonExistingUser").request().get();
        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testGetDialogsForUsername() throws Exception {
        String user = "participant";
        Client participant = new ClientBean(user, "project", Client.Role.DEV);
        DialogMessage expectedMessage = new DialogMessageBean(user, 0, "body");
        Dialog expectedDialog = createDummyDialog(DIALOG_ID, expectedMessage);
        Collection<Dialog> expectedDialogs = Collections.singleton(expectedDialog);

        when(mockClientService.getClient(eq(user))).thenReturn(participant);
        when(mockDialogService.getDialogsForParticipant(eq(participant))).thenReturn(expectedDialogs);

        final Response response = target("/tqm/rest/dialogs/users/" + user).request().get();
        final Dialog[] actualDialogs = response.readEntity(DialogBean[].class);

        assertThat(actualDialogs.length, is(1));
        assertThat(actualDialogs[0], equalTo(expectedDialog));
    }

    @Test
    public void testResolveDialogByUser() throws Exception {
        String user = "participant";
        Client participant = new ClientBean(user, "project", Client.Role.DEV);
        Dialog dialog = createDummyDialog(DIALOG_ID, new DialogMessageBean("user", 0, "body"));
        when(mockDialogService.getDialog(DIALOG_ID)).thenReturn(dialog);
        when(mockClientService.getClient(eq(user))).thenReturn(participant);

        DialogResolveRequestBean resolveBean = new DialogResolveRequestBean(user);

        final Response response = target("/tqm/rest/dialogs/" + DIALOG_ID + "/resolve").request()
                .post(Entity.entity(resolveBean, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200));
        verify(mockDialogService).markDialogAsResolved(eq(DIALOG_ID), eq(participant));
    }

    @Test
    public void testResolveDialogByUserNonExistingUser() throws Exception {
        Dialog dialog = createDummyDialog(DIALOG_ID, new DialogMessageBean("user", 0, "body"));
        when(mockDialogService.getDialog(DIALOG_ID)).thenReturn(dialog);

        DialogResolveRequestBean resolveBean = new DialogResolveRequestBean("not existing user");

        final Response response = target("/tqm/rest/dialogs/" + DIALOG_ID + "/resolve").request()
                .post(Entity.entity(resolveBean, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        verify(mockDialogService, never()).markDialogAsResolved(anyLong(), any());
    }

    @Test
    public void testResolveDialogByUserNonExistingDialog() throws Exception {
        String user = "participant";
        Client participant = new ClientBean(user, "project", Client.Role.DEV);
        when(mockClientService.getClient(eq(user))).thenReturn(participant);

        DialogResolveRequestBean resolveBean = new DialogResolveRequestBean(user);

        final Response response = target("/tqm/rest/dialogs/" + DIALOG_ID + "/resolve").request()
                .post(Entity.entity(resolveBean, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        verify(mockDialogService, never()).markDialogAsResolved(anyLong(), any());
    }

    private class TestBinder extends TqmBinder {

        public TestBinder(@NotNull PushService pushService) {
            super(pushService);
        }

        @Override
        protected void configure() {
            // Will have precedence
            bind(mockClientService).to(ClientService.class);
            bind(mockDialogService).to(DialogService.class);
            bind(mockPushService).to(PushService.class);
            bind(mockResponseWatcher).to(DialogResponseWatcherService.class);
            super.configure();
        }
    }

}
