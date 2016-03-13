package de.htwg.tqm.server.push;

import de.htwg.tqm.server.beans.Notification;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

/**
 * When connection is lost and a reconnect happens all outstanding push messages will be sent in order.
 */
@ThreadSafe
public interface PushService {

    /**
     * Tries to send the notification. If the notification could not be sent (i.e. no socket for the
     * user available) then it will be queued and resend later.
     */
    void send(@NotNull Notification notification);

}
