package de.htwg.tqm.server.poll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.JiraIssueBean;
import de.htwg.tqm.server.beans.JiraUserBean;
import de.htwg.tqm.server.beans.JiraIssue;
import de.htwg.tqm.server.jira.JiraService;
import de.htwg.tqm.server.beans.JiraUser;
import de.htwg.tqm.server.metric.MetricsService;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class JiraHoursPerUpdateCheckerTest {

    private static final String PROJECT1 = "proj1";
    private static final String USER1 = "user1";
    private static final String PROJECT2 = "proj2";
    private static final String USER2 = "user2";
    private static final String ISSUE_KEY = "iss1";
    private static final String ISSUE_NAME = "issue number one";
    private static final double ISSUE_HOURS_PER_UPDATE = 4.0;

    private JiraHoursPerUpdateChecker sut;
    private final ObjectMapper mapper = new ObjectMapper();
    private JiraService mockJiraService;

    @Before
    public void setUp() throws Exception {
        mockJiraService = mock(JiraService.class);

        Collection<JiraIssue> issues = new ArrayList<>();
        issues.add(new JiraIssueBean(ISSUE_KEY, ISSUE_NAME, USER1, ISSUE_HOURS_PER_UPDATE, MetricsService.Category.CRITICAL.toString(), ""));

        Collection<JiraUser> users = new ArrayList<>();
        users.add(new JiraUserBean(USER1, issues, MetricsService.Category.CRITICAL.toString()));

        when(mockJiraService.getIssues(anyString(), any())).thenReturn(issues);
        when(mockJiraService.getUsers(anyString(), any())).thenReturn(users);

        sut = new JiraHoursPerUpdateChecker(mockJiraService, () -> "");
    }

    @Test
    public void testCheck() throws Exception {
        final ObjectNode pushContent = createPushContent();
        final Collection<MetricViolation> expectedViolations = new ArrayList<>();
        expectedViolations.add(new MetricViolation("updateRateViolated", USER1, pushContent));

        final Collection<MetricViolation> actualViolations = sut.check(getFakeClients());
        verify(mockJiraService).getIssues(eq(PROJECT1), any());
        assertThat(actualViolations, equalTo(expectedViolations));
    }

    private @NotNull ObjectNode createPushContent() {
        final ObjectNode content = mapper.createObjectNode();

        final ObjectNode issue = content.putObject("issue");
        issue.put("key", ISSUE_KEY);
        issue.put("name", ISSUE_NAME);
        issue.put("hoursPerUpdate", ISSUE_HOURS_PER_UPDATE);

        return content;
    }

    private Map<String, String> getFakeClients() {
        Map<String, String> clients = new HashMap<>();
        clients.put(USER1, PROJECT1);
        clients.put(USER2, PROJECT2);
        return clients;
    }

}