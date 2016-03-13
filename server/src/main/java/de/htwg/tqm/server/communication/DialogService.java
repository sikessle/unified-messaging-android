package de.htwg.tqm.server.communication;

import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.beans.Dialog;
import de.htwg.tqm.server.beans.DialogMessage;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * High level interface to dialogs. A specific dialog handler routes the requests to the implemented service (JIRA, Email etc.)
 */
@ThreadSafe
public interface DialogService {

    /**
     * Creates a dialog.
     * @return The dialogID
     */
    long createDialog(@NotNull String subject, long violationID, @NotNull Client initiator, @NotNull Client affected);

    /**
     * Tries to retrieve the dialog for the dialogID
     */
    @Nullable Dialog getDialog(long dialogID);

    /**
     * Adds a message to a dialog
     */
    void addMessage(long dialogID, @NotNull DialogMessage message);

    /**
     * Returns all dialogs in which the participant is involved.
     */
    @NotNull Collection<Dialog> getDialogsForParticipant(@NotNull Client participant);

    /**
     * Checks if a dialog is existing for the specified violationID
     */
    boolean isDialogExistingForViolationID(long violationID);

    /**
     * Marks a dialog as resolved.
     */
    void markDialogAsResolved(long dialogID, @NotNull Client participant);
}
