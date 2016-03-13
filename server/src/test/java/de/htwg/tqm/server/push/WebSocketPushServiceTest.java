package de.htwg.tqm.server.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.ClientBean;
import de.htwg.tqm.server.beans.Notification;
import de.htwg.tqm.server.beans.NotificationBean;
import de.htwg.tqm.server.beans.Client;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class WebSocketPushServiceTest {

    private static final String USER = "myuser";
    private static final String PROJECT_1 = "myproject1";
    private static final String PROJECT_2 = "myproject2";
    private WebSocketPushService sut;
    private WebSocket socketSpy;
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        sut = new WebSocketPushService();
        socketSpy = spy(WebSocket.class);
    }

    private ObjectNode createAttachMessage(String projectKey) {
        ObjectNode message = mapper.createObjectNode();
        message.put("type", "attach");
        message.set("content", createContentNode(USER, projectKey));
        return message;
    }

    private ObjectNode createContentNode(String username, String projectKey) {
        final ObjectNode content = mapper.createObjectNode();
        content.put("user", username);
        content.put("project", projectKey);
        return content;
    }

    private void sendAttachMessage(@NotNull String username, @NotNull String projectKey,
                                   @NotNull String messageType) {
        sendAttachMessage(projectKey, messageType, createContentNode(username, projectKey));
    }

    private void sendAttachMessage(@NotNull String projectKey,
                                   @NotNull String messageType, @NotNull ObjectNode content) {
        final ObjectNode attachMessage = createAttachMessage(projectKey);
        attachMessage.put("type", messageType);
        attachMessage.set("content", content);

        sut.onMessage(socketSpy, attachMessage.toString());
    }

    @Test
    public void testOnMessageValidWithRegistration() throws Exception {
        sendAttachMessage(USER, PROJECT_1, "attach");
        assertThat(sut.hasSocket(USER, PROJECT_1), is(true));
    }

    @Test
    public void testOnMessageInvalidType() throws Exception {
        sendAttachMessage(USER, PROJECT_1, "wrong");
        assertThat(sut.hasSocket(USER, PROJECT_1), is(false));
    }

    @Test
    public void testOnMessageInvalidContent() throws Exception {
        ObjectNode content = createContentNode(USER, PROJECT_1);
        content.removeAll();
        sendAttachMessage(PROJECT_1, "attach", content);
        assertThat(sut.hasSocket(USER, PROJECT_1), is(false));
    }

    @Test
    public void testOnMessageInvalidFieldName() throws Exception {
        ObjectNode content = createContentNode(USER, PROJECT_1);
        content.removeAll();
        content.put("usernames", "wrongFieldName");
        sendAttachMessage(PROJECT_1, "attach", content);
        assertThat(sut.hasSocket(USER, PROJECT_1), is(false));
    }

    @Test
    public void testOnMessageProjectChange() throws Exception {
        sendAttachMessage(USER, PROJECT_1, "attach");
        sendAttachMessage(USER, PROJECT_2, "attach");

        assertThat(sut.hasSocket(USER, PROJECT_1), is(false));
        assertThat(sut.hasSocket(USER, PROJECT_2), is(true));
    }


    @Test
    public void testOnClose() throws Exception {
        sendAttachMessage(USER, PROJECT_1, "attach");
        assertThat(sut.hasSocket(USER, PROJECT_1), is(true));

        sut.onClose(socketSpy, mock(DataFrame.class));
        assertThat(sut.hasSocket(USER, PROJECT_1), is(false));
    }

    @Test
    public void testQueuedMessages() throws Exception {
        sendAttachMessage(USER, PROJECT_1, "attach");

        sut.onClose(socketSpy, mock(DataFrame.class));
        reset(socketSpy);

        final Notification notification = createNotification();
        final ObjectNode expectedMessage = createNotificationJson(notification);
        sut.send(notification);

        verify(socketSpy, never()).send(anyString());
        sendAttachMessage(USER, PROJECT_1, "attach");
        // Pending messages must be sent
        verify(socketSpy).send(expectedMessage.toString());
    }

    @Test
    public void testSend() throws Exception {
        sendAttachMessage(USER, PROJECT_1, "attach");
        Notification notification = createNotification();
        final ObjectNode expectedMessage = createNotificationJson(notification);
        sut.send(notification);
        verify(socketSpy).send(expectedMessage.toString());
    }

    private @NotNull ObjectNode createNotificationJson(@NotNull Notification notification) {
        final ObjectNode content = mapper.createObjectNode();
        content.put("type", notification.getType().toString());
        content.set("content", notification.getContent());
        return content;
    }

    private Notification createNotification() {
        return new NotificationBean(
                Notification.Type.metricViolation, Instant.now().toEpochMilli(), new ClientBean(USER, PROJECT_1, Client.Role.DEV),
                mapper.createObjectNode().put("key", "value"));
    }
}
