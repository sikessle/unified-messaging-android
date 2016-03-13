package de.htwg.tqm.server.push;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.beans.Notification;
import net.jcip.annotations.ThreadSafe;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@ThreadSafe
@Singleton
public final class WebSocketPushService extends WebSocketApplication implements PushService {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(WebSocketPushService.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, ProjectSocket> userToProjectSocket = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> socketToUser = new ConcurrentHashMap<>();
    private final Queue<PendingMessage> pendingMessages = new ConcurrentLinkedDeque<>();

    @Override
    public void onMessage(WebSocket socket, String message) {
        try {
            final JsonNode jsonNode = mapper.readTree(message);
            if (isValidMessage(jsonNode)) {
                processMessage(socket, jsonNode);
            } else {
                LOG.warn("Invalid message received: {}", message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidMessage(@NotNull JsonNode jsonNode) {
        return !jsonNode.path("type").isMissingNode()
                && jsonNode.get("type").asText().equals("attach")
                && jsonNode.path("content").path("user").isValueNode()
                && jsonNode.path("content").path("project").isValueNode();
    }

    private void processMessage(@NotNull WebSocket socket, @NotNull JsonNode jsonNode) {
        final String username = jsonNode.path("content").path("user").asText();
        final String projectKey = jsonNode.path("content").path("project").asText();

        storeSocket(socket, username, projectKey);
        sendPendingMessages(username, projectKey);
    }


    private void storeSocket(@NotNull WebSocket socket, @NotNull String username, @NotNull String projectKey) {
        userToProjectSocket.put(username, new ProjectSocket(projectKey, socket));
        socketToUser.put(socket, username);
        LOG.debug("Socket attached: user: {}, project: {}", username, projectKey);
    }

    private void sendPendingMessages(@NotNull String username, @NotNull String projectKey) {
        final Iterator<PendingMessage> iterator = pendingMessages.iterator();

        while (iterator.hasNext()) {
            if (sendPendingMessageIfMatches(username, projectKey, iterator.next())) {
                LOG.debug("Queue message sent to {}", username);
                iterator.remove();
            }
        }
    }

    private boolean sendPendingMessageIfMatches(@NotNull String username, @NotNull String projectKey,
                                                @NotNull PendingMessage pendingMsg) {
        final Client receiver = pendingMsg.getMessage().getReceiver();
        if (receiver.getName().equals(username) && receiver.getProject().equals(projectKey)) {
            send(pendingMsg.getMessage());
            return true;
        }
        return false;
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        super.onClose(socket, frame);
        final String user = socketToUser.get(socket);
        if (user != null) {
            userToProjectSocket.remove(user);
            LOG.debug("Socket closed for user: {}", user);
        }
    }

    boolean hasSocket(@NotNull String username, @NotNull String projectKey) {
        final ProjectSocket projectSocket = userToProjectSocket.get(username);
        return projectSocket != null && projectSocket.projectKey.equals(projectKey);
    }

    @Override
    public void send(@NotNull Notification notification) {
        final String projectKey = notification.getReceiver().getProject();
        final String username = notification.getReceiver().getName();
        final ProjectSocket projectSocket = userToProjectSocket.get(username);

        if (projectSocket != null && projectSocket.projectKey.equals(projectKey)) {
            WebSocket socket = projectSocket.socket;
            ObjectNode jsonNotification = createNotificationJson(notification);
            socket.send(jsonNotification.toString());
            LOG.debug("Push message sent to {}: {}", username, jsonNotification.toString());
        } else {
            pendingMessages.add(new PendingMessage(notification));
            LOG.debug("Push message queued as pending message of {}", username);
        }
    }


    private @NotNull ObjectNode createNotificationJson(@NotNull Notification notification) {
        final ObjectNode message = mapper.createObjectNode();
        message.put("type", notification.getType().toString());
        message.set("content", notification.getContent());
        return message;
    }

    private static class ProjectSocket {
        private final String projectKey;
        private final WebSocket socket;

        public ProjectSocket(@NotNull String projectKey, @NotNull WebSocket socket) {
            this.projectKey = projectKey;
            this.socket = socket;
        }
    }
}
