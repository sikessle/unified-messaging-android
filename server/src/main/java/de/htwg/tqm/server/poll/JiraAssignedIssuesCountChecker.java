package de.htwg.tqm.server.poll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.jira.Authentication;
import de.htwg.tqm.server.metric.MetricsService;
import de.htwg.tqm.server.jira.JiraService;
import de.htwg.tqm.server.beans.JiraUser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.*;

final class JiraAssignedIssuesCountChecker {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(JiraAssignedIssuesCountChecker.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final JiraService jiraService;
    private final Authentication auth;
    private final Collection<MetricViolation> violations;
    private final static String VIOLATION_NAME = "assignedIssuesCountViolated";

    public JiraAssignedIssuesCountChecker(@NotNull JiraService jiraService, @NotNull Authentication auth) {
        this.jiraService = jiraService;
        this.auth = auth;
        this.violations = new ArrayList<>();
    }

    /**
     * @param clientNameToProject (Username -> Project)
     */
    public @NotNull Collection<MetricViolation> check(@NotNull Map<String, String> clientNameToProject) {
        violations.clear();

        Set<String> projectKeys = new HashSet<>(clientNameToProject.values());

        projectKeys.forEach(projectKey -> {
            final Collection<JiraUser> users = jiraService.getUsers(projectKey, auth);
            checkUsersForProject(clientNameToProject, users, projectKey);
        });

        return violations;
    }

    private void checkUsersForProject(@NotNull Map<String, String> clientNameToProject, @NotNull Collection<JiraUser> users,
                                      @NotNull String projectKey) {
        users.forEach(user -> {
            LOG.debug("Checking for assignedIssuesCountViolation, user: {}", user.getName());
            String name = user.getName();
            if (clientNameToProject.containsKey(name)
                    && clientNameToProject.get(name).equals(projectKey)
                    && isCritical(user.getAssignedIssuesCountMetricCategory())) {
                violations.add(createMetricViolation(user));
            }
        });
    }

    private MetricViolation createMetricViolation(@NotNull JiraUser user) {
        final ObjectNode content = mapper.createObjectNode();
        ArrayNode issuesContent = content.putArray("issues");
        user.getAssignedIssues().forEach(issue -> {
            final ObjectNode issueObject = issuesContent.addObject();
            issueObject.put("key", issue.getKey());
            issueObject.put("name", issue.getName());
        });
        content.put("count", user.getAssignedIssuesCount());

        return new MetricViolation(VIOLATION_NAME, user.getName(), content);
    }

    private boolean isCritical(@NotNull String category) {
        return category.equals(MetricsService.Category.CRITICAL.toString());
    }
}
