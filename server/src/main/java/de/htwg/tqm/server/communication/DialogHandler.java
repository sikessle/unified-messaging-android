package de.htwg.tqm.server.communication;

import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.beans.DialogMessage;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;

@ThreadSafe
public interface DialogHandler {

    /**
     * @return Must return a unique identifier which identifies the specific implementation of this interface.
     */
    @NotNull String getUniqueHandlerIdentifier();

    /**
     * @return A unique key to identify the dialog (issue)
     */
    @NotNull String createDialog(@NotNull String subject, @NotNull Client initiator, @NotNull Client affected);

    /**
     * Returns all messages of the specified dialog
     */
    @NotNull SortedSet<DialogMessage> getDialogMessages(@NotNull String dialogKey);

    /**
     * Adds a message to the dialog
     */
    void addMessage(@NotNull String dialogKey, @NotNull DialogMessage message);

    /**
     * Callback which is called by the dialog service to notify the dialog handler that a particular dialog
     * has been marked as resolved. It is up to the implementation how this is handled.
     * An implementation could i.e. close the issue, archive an email or chat.
     */
    void onMarkDialogAsResolved(@NotNull String dialogKey);
}
