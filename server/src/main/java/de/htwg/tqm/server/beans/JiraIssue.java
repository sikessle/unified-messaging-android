package de.htwg.tqm.server.beans;

import org.jetbrains.annotations.NotNull;

public interface JiraIssue {

    @NotNull String getKey();

    @NotNull String getAssignee();

    @NotNull String getName();

    double getHoursPerUpdate();

    @NotNull String getUpdateRateMetricCategory();

    @NotNull String getLink();
}
