package de.htwg.tqm.server.metric;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@ThreadSafe
@Singleton
public final class DefaultMetricsService implements MetricsService {

    private final double issueOK;
    private final double issueWARN;
    private final int userOK;
    private final int userWARN;

    @Inject
    public DefaultMetricsService(@Named("hoursPerUpdateOK") double issueOK,
                                 @Named("hoursPerUpdateWARN") double issueWARN,
                                 @Named("assignedIssuesOK") int userOK,
                                 @Named("assignedIssuesWARN") int userWARN
    ) {
        this.issueOK = issueOK;
        this.issueWARN = issueWARN;
        this.userOK = userOK;
        this.userWARN = userWARN;
    }

    @Override
    public @NotNull Category categoryOfIssue(double hoursPerUpdate) {
        return categoriyOfNumber(hoursPerUpdate, issueOK, issueWARN);
    }

    @Override
    public @NotNull Category categoryOfUser(int assignedIssuesCount) {
        return categoriyOfNumber(assignedIssuesCount, userOK, userWARN);
    }

    private @NotNull Category categoriyOfNumber(@NotNull Number value, @NotNull Number thresholdOK, @NotNull Number thresholdWARN) {
        if (value.doubleValue() <= thresholdOK.doubleValue()) {
            return Category.OK;
        } else if (value.doubleValue() <= thresholdWARN.doubleValue()) {
            return Category.WARN;
        }
        return Category.CRITICAL;
    }
}
