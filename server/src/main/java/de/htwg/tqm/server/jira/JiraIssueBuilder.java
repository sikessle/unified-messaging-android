package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.JiraIssue;
import de.htwg.tqm.server.beans.JiraIssueBean;
import de.htwg.tqm.server.metric.MetricsService;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

@ThreadSafe
final class JiraIssueBuilder {

    private final MetricsService categorizer;
    private final String jiraIssueUrlPrefix;

    public JiraIssueBuilder(@NotNull MetricsService categorizer, @NotNull URL jiraIssueUrlPrefix) {
        this.categorizer = categorizer;
        URL prefix = jiraIssueUrlPrefix;
        try {
            prefix = new URL(jiraIssueUrlPrefix, "browse/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.jiraIssueUrlPrefix = prefix.toString();
    }

    public @NotNull JiraIssue fromIssueResponse(@NotNull JiraIssueResponseBean response) {
        return new JiraIssueBean(response.getKey(),
                response.getName(),
                response.getAssignee(),
                response.getHoursPerUpdate(),
                categorizer.categoryOfIssue(response.getHoursPerUpdate()).toString(),
                jiraIssueUrlPrefix + response.getKey()
        );
    }

}
