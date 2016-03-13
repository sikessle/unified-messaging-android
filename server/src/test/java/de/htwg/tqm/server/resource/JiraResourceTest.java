package de.htwg.tqm.server.resource;

import de.htwg.tqm.server.ServerTest;
import de.htwg.tqm.server.TqmBinder;
import de.htwg.tqm.server.beans.JiraIssueBean;
import de.htwg.tqm.server.beans.JiraProjectBean;
import de.htwg.tqm.server.beans.JiraUserBean;
import de.htwg.tqm.server.jira.Authentication;
import de.htwg.tqm.server.beans.JiraIssue;
import de.htwg.tqm.server.beans.JiraProject;
import de.htwg.tqm.server.jira.JiraService;
import de.htwg.tqm.server.beans.JiraUser;
import de.htwg.tqm.server.metric.MetricsService;
import de.htwg.tqm.server.push.PushService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JiraResourceTest extends ServerTest {

    private final JiraService mockJiraService = mock(JiraService.class);

    @Override
    protected @Nullable AbstractBinder getBinder() {
        return new TestBinder(mock(PushService.class));
    }

    @Test
    public void testGetIssues() throws Exception {
        final String projectKey = "myKey";
        final Collection<JiraIssue> expectedIssues = new ArrayList<>();
        expectedIssues.add(new JiraIssueBean("key", "name", "assignee", 2.0, MetricsService.Category.OK.toString(), ""));

        when(mockJiraService.getIssues(anyString(), any())).thenReturn(expectedIssues);

        final Response response = target("/tqm/rest/metrics/jira/projects/" + projectKey + "/issues")
                .request().header("Authorization", getFakeAuth()).get();

        final List<JiraIssueBean> actualIssues = Arrays.asList(response.readEntity(JiraIssueBean[].class));

        assertThat(response.getStatus(), is(200));
        assertThat(actualIssues.size(), equalTo(expectedIssues.size()));
        assertTrue(actualIssues.containsAll(expectedIssues));
    }

    @Test
    public void testAuthHeader() throws Exception {
        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        when(mockJiraService.getIssues(anyString(), captor.capture())).thenReturn(Collections.emptyList());
        target("/tqm/rest/metrics/jira/projects/projectKey/issues").request().header("Authorization", getFakeAuth()).get();
        assertThat(captor.getValue().getBasicAuthValue(), equalTo(getFakeAuth()));
    }

    private @NotNull String getFakeAuth() {
        return "Basic base64encoded(user:pass)";
    }

    @Test
    public void testGetProjects() throws Exception {
        final Collection<JiraProject> expectedProjects = new ArrayList<>();
        expectedProjects.add(new JiraProjectBean("key", "name"));

        when(mockJiraService.getProjects(any())).thenReturn(expectedProjects);

        final Response response = target("/tqm/rest/metrics/jira/projects/").request().header("Authorization", getFakeAuth()).get();

        final List<JiraProjectBean> actualProjects = Arrays.asList(response.readEntity(JiraProjectBean[].class));

        assertThat(response.getStatus(), is(200));
        assertThat(actualProjects.size(), equalTo(expectedProjects.size()));
        assertTrue(actualProjects.containsAll(expectedProjects));
    }

    @Test
    public void testGetUsers() throws Exception {
        final Collection<JiraUser> expectedUsers = new ArrayList<>();
        JiraIssue expectedAssignedIssue = new JiraIssueBean("key", "name", "assignee", 2.2, MetricsService.Category.CRITICAL.toString(), "localhost");
        JiraUser expectedUser = new JiraUserBean("key", Collections.singleton(expectedAssignedIssue), MetricsService.Category.OK.toString());
        expectedUsers.add(expectedUser);

        when(mockJiraService.getUsers(anyString(), any())).thenReturn(expectedUsers);

        final Response response = target("/tqm/rest/metrics/jira/projects/projectKey/users")
                .request().header("Authorization", getFakeAuth()).get();

        final List<JiraUserBean> actualUsers = Arrays.asList(response.readEntity(JiraUserBean[].class));

        assertThat(response.getStatus(), is(200));
        assertThat(actualUsers.size(), is(1));

        final JiraUserBean actualUser = actualUsers.get(0);

        assertThat(actualUser, equalTo(expectedUser));
    }

    private class TestBinder extends TqmBinder {

        public TestBinder(@NotNull PushService pushService) {
            super(pushService);
        }

        @Override
        protected void configure() {
            // Will have precedence
            bind(mockJiraService).to(JiraService.class);
            super.configure();
        }
    }
}