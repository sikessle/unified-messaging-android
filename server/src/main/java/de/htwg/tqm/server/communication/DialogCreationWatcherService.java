package de.htwg.tqm.server.communication;

import de.htwg.tqm.server.beans.MissingDialog;
import de.htwg.tqm.server.beans.Notification;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@ThreadSafe
public interface DialogCreationWatcherService {

    /**
     * Watches if a dialog is created in-time for the violationID.
     */
    void watchViolation(long violationID, @NotNull Notification notification);

    /**
     * @return All dialogs which should but have not been created.
     */
    @NotNull Collection<MissingDialog> getMissingDialogs();

}
