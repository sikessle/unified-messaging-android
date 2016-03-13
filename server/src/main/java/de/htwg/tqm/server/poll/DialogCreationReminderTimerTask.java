package de.htwg.tqm.server.poll;

import de.htwg.tqm.server.beans.NotificationBean;
import de.htwg.tqm.server.beans.Notification;
import de.htwg.tqm.server.communication.DialogCreationWatcherService;
import de.htwg.tqm.server.push.PushService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.TimerTask;

final class DialogCreationReminderTimerTask extends TimerTask {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(DialogCreationReminderTimerTask.class);

    private final PushService pushService;
    private final DialogCreationWatcherService creationWatcher;

    public DialogCreationReminderTimerTask(@NotNull DialogCreationWatcherService creationWatcher,
                                           @NotNull PushService pushService) {
        this.pushService = pushService;
        this.creationWatcher = creationWatcher;
    }

    @Override
    public void run() {
        try {
            creationWatcher.getMissingDialogs().forEach(missingDialog -> resendNotification(missingDialog.getNotification()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resendNotification(@NotNull  Notification oldNotification) {
        LOG.debug("Resending notification for creating a dialog: {}", oldNotification);
        pushService.send(new NotificationBean(oldNotification.getType(), Instant.now().toEpochMilli(),
                oldNotification.getReceiver(), oldNotification.getContent()));
    }


}
