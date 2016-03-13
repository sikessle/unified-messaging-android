package de.htwg.tqm.server.communication;

import de.htwg.tqm.server.beans.Dialog;
import de.htwg.tqm.server.beans.DialogMessage;
import de.htwg.tqm.server.beans.MissingResponse;
import de.htwg.tqm.server.beans.MissingResponseBean;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

@ThreadSafe
@Singleton
public final class SimpleDialogResponseWatcherService implements DialogResponseWatcherService {

    private final DialogService dialogService;
    private final Set<Long> dialogIDs = new ConcurrentSkipListSet<>();
    private final Duration durationBeforeRemindForDialogResponse;

    @Inject
    public SimpleDialogResponseWatcherService(@NotNull DialogService dialogService,
                                              @Named("durationBeforeRemindForDialogResponse") @NotNull Duration durationBeforeRemindForDialogResponse)

    {
        this.dialogService = dialogService;
        this.durationBeforeRemindForDialogResponse = durationBeforeRemindForDialogResponse;
    }

    @Override
    public void watchDialog(long dialogID) {
        dialogIDs.add(dialogID);
    }

    @Override
    public @NotNull Collection<MissingResponse> getMissingResponses() {
        Collection<MissingResponse> missingResponses = new ArrayList<>();

        final Iterator<Long> it = dialogIDs.iterator();

        while (it.hasNext()) {
            long dialogID = it.next();
            Dialog dialog = dialogService.getDialog(dialogID);
            if (dialog != null) {
                MissingResponse missing = checkDialog(it, dialog);
                if (missing != null) {
                    missingResponses.add(missing);
                }
            }
        }

        return missingResponses;
    }

    private @Nullable MissingResponse checkDialog(@NotNull Iterator<Long> it, @NotNull Dialog dialog) {
        // Dialog was resolved
        if (dialog.getResolvedAffected() && dialog.getResolvedInitiator()) {
            it.remove();
            return null;
        }
        return getIfMissingResponse(dialog);
    }

    private @Nullable MissingResponse getIfMissingResponse(@NotNull Dialog dialog) {
        final SortedSet<DialogMessage> messages = dialog.getMessages();
        long now = Instant.now().toEpochMilli();

        if (messages.isEmpty()) {
            Duration durationBetweenDialogCreation = Duration.ofMillis(now - dialog.getTimestamp());
            if (durationBetweenDialogCreation.compareTo(durationBeforeRemindForDialogResponse) >= 0) {
                return new MissingResponseBean(dialog.getDialogID(), dialog.getAffected());
            } else {
                return null;
            }
        } else {
            DialogMessage lastMessage = messages.last();
            Duration durationSinceLastResponse = Duration.ofMillis(now - lastMessage.getTimestamp());

            if (durationSinceLastResponse.compareTo(durationBeforeRemindForDialogResponse) >= 0) {
                String userWhoDidNotRespond;
                if (dialog.getInitiator().equals(lastMessage.getAuthor())) {
                    userWhoDidNotRespond = dialog.getAffected();
                } else {
                    userWhoDidNotRespond = dialog.getInitiator();
                }
                return new MissingResponseBean(dialog.getDialogID(), userWhoDidNotRespond);
            }

            return null;
        }
    }
}
