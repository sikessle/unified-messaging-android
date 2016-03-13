package de.htwg.tqm.server.communication;

import de.htwg.tqm.server.beans.MissingResponse;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@ThreadSafe
public interface DialogResponseWatcherService {

    /**
     * Watches a dialog for in-time responses of the communication partners.
     */
    void watchDialog(long dialogID);

    /**
     * Returns all missing responses which were found (that means, a dialog partner has not responded in-time to the
     * other communication partner)
     */
    @NotNull Collection<MissingResponse>  getMissingResponses();

}
