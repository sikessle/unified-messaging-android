package de.htwg.tqm.server.metric;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

/**
 * Categorizes based on one or multiple KPIs and returns an appropriate Category.
 */
@ThreadSafe
public interface MetricsService {

    /**
     * Rates an issue based on its hours per update.
     */
    @NotNull Category categoryOfIssue(double hoursPerUpdate);

    /**
     * Rates an user based on its number of assigned issues.
     */
    @NotNull Category categoryOfUser(int assignedIssuesCount);

    enum Category {
        OK, WARN, CRITICAL
    }
}
