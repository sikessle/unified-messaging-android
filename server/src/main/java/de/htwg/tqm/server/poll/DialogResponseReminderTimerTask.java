package de.htwg.tqm.server.poll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.NotificationBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.beans.Notification;
import de.htwg.tqm.server.communication.DialogResponseWatcherService;
import de.htwg.tqm.server.push.PushService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.TimerTask;

final class DialogResponseReminderTimerTask extends TimerTask {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(DialogResponseReminderTimerTask.class);

    private final PushService pushService;
    private final ClientService clientService;
    private final DialogResponseWatcherService responseWatcher;
    private final ObjectMapper mapper = new ObjectMapper();


    public DialogResponseReminderTimerTask(@NotNull DialogResponseWatcherService responseWatcher,
                                           @NotNull PushService pushService,
                                           @NotNull ClientService clientService) {
        this.responseWatcher = responseWatcher;
        this.pushService = pushService;
        this.clientService = clientService;
    }

    @Override
    public void run() {
        responseWatcher.getMissingResponses().forEach(missingResponse -> {
            LOG.debug("Missing dialog response from {} for dialogID {}", missingResponse.getUserWhoDidNotRespond(), missingResponse.getDialogID());
            ObjectNode content = mapper.createObjectNode();
            content.put("dialogID", missingResponse.getDialogID());
            Client receiver = clientService.getClient(missingResponse.getUserWhoDidNotRespond());

            if (receiver != null) {
                pushService.send(new NotificationBean(Notification.Type.missingDialogResponse,
                        Instant.now().toEpochMilli(), receiver, content));
            } else {
                LOG.warn("No client registered for username: {}", missingResponse.getUserWhoDidNotRespond());
            }
        });
    }


}
