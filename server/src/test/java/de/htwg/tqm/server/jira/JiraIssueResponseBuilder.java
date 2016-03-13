package de.htwg.tqm.server.jira;

import org.jetbrains.annotations.NotNull;

final class JiraIssueResponseBuilder {

    public static @NotNull JiraIssueResponseBean buildDummy(@NotNull String key, @NotNull String name, @NotNull String assigneeName, int worklogTotal) {

        JiraIssueResponseBean.GenericFields rootFields = new JiraIssueResponseBean.GenericFields();
        JiraIssueResponseBean.GenericFields assigneeFields = new JiraIssueResponseBean.GenericFields();
        JiraIssueResponseBean.GenericFields timetrackingFields = new JiraIssueResponseBean.GenericFields();
        JiraIssueResponseBean.GenericFields worklogFields = new JiraIssueResponseBean.GenericFields();

        rootFields.summary = name;
        rootFields.assignee = assigneeFields;
        rootFields.assignee.name = assigneeName;
        rootFields.timetracking = timetrackingFields;
        rootFields.timetracking.timeSpentSeconds = 60 * 60;
        rootFields.worklog = worklogFields;
        rootFields.worklog.total = worklogTotal;

        return new JiraIssueResponseBean(key, rootFields);
    }

}
