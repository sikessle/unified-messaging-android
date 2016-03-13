package de.htwg.tqm.server.poll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.jira.Authentication;
import de.htwg.tqm.server.metric.MetricsService;
import de.htwg.tqm.server.beans.JiraIssue;
import de.htwg.tqm.server.jira.JiraService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

final class JiraHoursPerUpdateChecker {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JiraService jiraService;
    private final Authentication auth;
    private final Collection<MetricViolation> violations;
    private final static String VIOLATION_NAME = "updateRateViolated";

    public JiraHoursPerUpdateChecker(@NotNull JiraService jiraService, @NotNull Authentication auth) {
        this.jiraService = jiraService;
        this.auth = auth;
        this.violations = new ArrayList<>();
    }

    /**
     * @param clientNameToProject (Username -> Project)
     */
    public @NotNull Collection<MetricViolation> check(@NotNull Map<String, String> clientNameToProject) {
        violations.clear();

        clientNameToProject.values().forEach(projectKey -> {
            final Collection<JiraIssue> issues = jiraService.getIssues(projectKey, auth);
            checkIssuesForProject(clientNameToProject, issues, projectKey);
        });

        return violations;
    }

    private void checkIssuesForProject(@NotNull Map<String, String> clientNameToProject, @NotNull Collection<JiraIssue> issues,
                                       @NotNull String projectKey) {
        issues.forEach(issue -> {
            String assignee = issue.getAssignee();
            if (clientNameToProject.containsKey(assignee)
                    && clientNameToProject.get(assignee).equals(projectKey)
                    && isCritical(issue.getUpdateRateMetricCategory())) {
                violations.add(createMetricViolation(issue));
            }
        });
    }

    private MetricViolation createMetricViolation(@NotNull JiraIssue issue) {
        final ObjectNode content = mapper.createObjectNode();
        ObjectNode issueContent = content.putObject("issue");
        issueContent.put("key", issue.getKey());
        issueContent.put("name", issue.getName());
        issueContent.put("hoursPerUpdate", issue.getHoursPerUpdate());

        return new MetricViolation(VIOLATION_NAME, issue.getAssignee(), content);
    }

    private boolean isCritical(@NotNull String category) {
        return category.equals(MetricsService.Category.CRITICAL.toString());
    }

}
