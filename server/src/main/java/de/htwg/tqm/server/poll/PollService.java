package de.htwg.tqm.server.poll;

import net.jcip.annotations.NotThreadSafe;

/**
 * Periodically scans a resource (i.e. JIRA) and checks for violated metrics or other triggers and
 * notifies the push service about it.
 */
@NotThreadSafe
public interface PollService {

    /**
     * Starts the poll service if it is not yet running.
     */
    void start();

    /**
     * Cancels all current running scans and quits the poll service if it is running.
     */
    void shutdown();

}
