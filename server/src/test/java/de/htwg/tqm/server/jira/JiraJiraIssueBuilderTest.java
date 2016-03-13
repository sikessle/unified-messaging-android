package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.JiraIssue;
import de.htwg.tqm.server.metric.MetricsService;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JiraJiraIssueBuilderTest {

    private static final String JIRA_BASE_URL = "http://localhost";
    private JiraIssueBuilder sut;


    @Before
    public void setUp() throws Exception {
        MetricsService mockCategorizer = mock(MetricsService.class);
        when(mockCategorizer.categoryOfIssue(anyDouble())).thenReturn(MetricsService.Category.OK);
        sut = new JiraIssueBuilder(mockCategorizer, new URL(JIRA_BASE_URL));
    }

    @Test
    public void testFromIssueResponse() throws Exception {
        String key = "key";
        String assigneeSuffix = "-assignee";
        final JiraIssueResponseBean issueResponse = JiraIssueResponseBuilder.buildDummy(key, "", assigneeSuffix, 2);
        final JiraIssue issue = sut.fromIssueResponse(issueResponse);

        assertThat(issue.getKey(), equalTo(key));
        assertThat(issue.getName(), equalTo(issueResponse.getName()));
        assertThat(issue.getAssignee(), equalTo(issueResponse.getAssignee()));
        assertThat(issue.getHoursPerUpdate(), equalTo(issueResponse.getHoursPerUpdate()));
        assertThat(issue.getUpdateRateMetricCategory(), equalTo(MetricsService.Category.OK.toString()));
        assertThat(issue.getLink(), equalTo(JIRA_BASE_URL + "/browse/" + issue.getKey()));
    }

    @Test
    public void testFromIssueResponseNoWorklogItems() throws Exception {
        final JiraIssueResponseBean issueResponse = JiraIssueResponseBuilder.buildDummy("key", "", "", 0);
        final JiraIssue issue = sut.fromIssueResponse(issueResponse);
        assertThat(issue.getHoursPerUpdate(), equalTo(0.0));
    }
}