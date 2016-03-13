package de.htwg.tqm.server.communication;

import de.htwg.tqm.server.beans.MissingDialog;
import de.htwg.tqm.server.beans.MissingDialogBean;
import de.htwg.tqm.server.beans.Notification;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
@Singleton
public final class SimpleDialogCreationWatcherService implements DialogCreationWatcherService {

    private final Map<Long, Notification> violationIDsToNotification = new ConcurrentHashMap<>();
    private final Duration durationBeforeRemindForDialogCreation;
    private final DialogService dialogService;

    @Inject
    public SimpleDialogCreationWatcherService(@NotNull DialogService dialogService,
                                              @NotNull @Named("durationBeforeRemindForDialogCreation") Duration durationBeforeRemindForDialogCreation) {
        this.dialogService = dialogService;
        this.durationBeforeRemindForDialogCreation = durationBeforeRemindForDialogCreation;
    }

    @Override
    public void watchViolation(long violationID, @NotNull Notification notification) {
        violationIDsToNotification.put(violationID, notification);
    }

    @Override
    public @NotNull Collection<MissingDialog> getMissingDialogs() {
        Collection<MissingDialog> missingDialogs = new LinkedList<>();

        final Iterator<Map.Entry<Long, Notification>> it = violationIDsToNotification.entrySet().iterator();

        while (it.hasNext()) {
            MissingDialog missing = getIfMissingDialogCreation(it);
            if (missing != null) {
                missingDialogs.add(missing);
            }
        }

        return missingDialogs;
    }

    private @Nullable MissingDialog getIfMissingDialogCreation(@NotNull Iterator<Map.Entry<Long, Notification>> it) {
        final Map.Entry<Long, Notification> entry = it.next();
        long violationID = entry.getKey();
        Notification notification = entry.getValue();

        if (dialogService.isDialogExistingForViolationID(violationID)) {
            it.remove();
            return null;
        }

        long now = Instant.now().toEpochMilli();
        Duration durationSinceNotification = Duration.ofMillis(now - notification.getTimestamp());

        if (durationSinceNotification.compareTo(durationBeforeRemindForDialogCreation) >= 0) {
            return new MissingDialogBean(notification);
        }

        return null;
    }
}
