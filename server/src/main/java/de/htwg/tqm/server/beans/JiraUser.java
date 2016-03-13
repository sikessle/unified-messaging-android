package de.htwg.tqm.server.beans;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface JiraUser {

    @NotNull String getName();

    @NotNull Collection<JiraIssue> getAssignedIssues();

    int getAssignedIssuesCount();

    @NotNull String getAssignedIssuesCountMetricCategory();

}
