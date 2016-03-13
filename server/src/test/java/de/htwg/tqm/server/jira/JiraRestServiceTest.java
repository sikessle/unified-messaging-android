package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.*;
import de.htwg.tqm.server.metric.MetricsService;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.ws.http.HTTPException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JiraRestServiceTest {

    private JiraRestService sut;
    private Authentication auth;
    private Response mockResponse;
    private Client mockClient;
    private Invocation.Builder mockInvocationBuilder;
    private ArgumentCaptor<String> urlCaptor;

    @Before
    public void setUp() throws Exception {
        auth = () -> "Basic basee64encoded(user:pass)";
        mockClient = mock(Client.class);
        WebTarget mockTarget = mock(WebTarget.class);
        mockInvocationBuilder = mock(Invocation.Builder.class);
        mockResponse = mock(Response.class);
        MetricsService mockCategorizer = mock(MetricsService.class);

        urlCaptor = ArgumentCaptor.forClass(String.class);
        when(mockClient.target(urlCaptor.capture())).thenReturn(mockTarget);
        when(mockTarget.request()).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.header(anyString(), anyObject())).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.accept(anyString())).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockInvocationBuilder.post(any())).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockCategorizer.categoryOfIssue(anyDouble())).thenReturn(MetricsService.Category.CRITICAL);
        when(mockCategorizer.categoryOfUser(anyInt())).thenReturn(MetricsService.Category.CRITICAL);


        URL jiraBaseUrl = new URL("http://localhost");
        sut = new JiraRestService(mockClient, jiraBaseUrl, mockCategorizer);

    }

    private void checkCapturedUrl() throws MalformedURLException, URISyntaxException {
        // May throw an exception if the URL is invalid
        final URL url = new URL(urlCaptor.getValue());
        url.toURI();
    }

    @Test
    public void testGetIssues() throws Exception {
        Set<String> expectedIssueKeys = getIssueKeys();
        configMockResponseReturnJqlResponse(expectedIssueKeys, "");

        final Collection<JiraIssue> actualIssues = sut.getIssues("projectKey", auth);
        checkCapturedUrl();
        assertThat(actualIssues.size(), is(expectedIssueKeys.size()));
        actualIssues.forEach(actualIssue -> assertTrue(expectedIssueKeys.contains(actualIssue.getKey())));
    }

    @Test(expected = HTTPException.class)
    public void testGetIssuesFailedLogin() throws Exception {
        testFailedAuth(v -> sut.getIssues("projectKey", auth));
    }

    @Test
    public void testGetUsers() throws Exception {
        String suffix = "-assignee";
        Set<String> expectedIssueKeys = getIssueKeys();
        Set<String> expectedUserNames = new HashSet<>();
        expectedIssueKeys.forEach(issueKey -> expectedUserNames.add(issueKey + suffix));
        // To get users the service is utilizing the getIssues method
        configMockResponseReturnJqlResponse(expectedIssueKeys, suffix);

        final Collection<JiraUser> users = sut.getUsers("projectKey", auth);
        checkCapturedUrl();
        // Each issue has its own user in this test
        assertThat(users.size(), is(expectedIssueKeys.size()));
        users.forEach(user -> assertTrue(expectedUserNames.contains(user.getName())));
    }

    @Test(expected = HTTPException.class)
    public void testGetUsersFailedLogin() throws Exception {
        testFailedAuth(v -> sut.getUsers("projectKey", auth));
    }


    private void configMockResponseReturnJqlResponse(@NotNull Set<String> expectedIssueKeys, @NotNull String assigneeSuffix) {
        JqlResponseBean jqlResponse = prepareJqlResponse(expectedIssueKeys, assigneeSuffix);
        when(mockResponse.readEntity(JqlResponseBean.class)).thenReturn(jqlResponse);
    }

    private @NotNull Set<String> getIssueKeys() {
        Set<String> keys = new HashSet<>();
        keys.add("iss1");
        keys.add("iss2");
        return keys;
    }


    @Test
    public void testGetProjects() throws Exception {
        final JiraProjectBean[] expectedProjects = new JiraProjectBean[]{
                new JiraProjectBean("key1", "Full name"),
                new JiraProjectBean("key2", "Full name")
        };

        when(mockResponse.readEntity(JiraProjectBean[].class)).thenReturn(expectedProjects);
        final Collection<JiraProject> actualProjects = sut.getProjects(auth);
        checkCapturedUrl();

        assertThat(actualProjects.size(), is(expectedProjects.length));
        for (JiraProjectBean expected : expectedProjects) {
            assertTrue(actualProjects.contains(expected));
        }
    }

    @Test(expected = HTTPException.class)
    public void testGetProjectsFailedLogin() throws Exception {
        testFailedAuth(v -> sut.getProjects(auth));
    }

    private void testFailedAuth(Function<Void, Object> sutCall) throws HTTPException {
        when(mockResponse.getStatus()).thenReturn(404);
        sutCall.apply(null);
    }

    @Test
    public void testCreateIssue() throws Exception {
        when(mockResponse.getStatus()).thenReturn(201);
        JiraIssueResponseBean issueResponse = JiraIssueResponseBuilder.buildDummy("key", "name", "assignee", 0);
        when(mockResponse.readEntity(JiraIssueResponseBean.class)).thenReturn(issueResponse);
        ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);

        String issueKey = sut.createIssue("name", "description", "project", "assignee", "Task", auth);
        checkCapturedUrl();
        verify(mockClient).target(argThat(org.hamcrest.Matchers.endsWith("rest/api/2/issue/")));

        assertEquals(issueKey, issueResponse.getKey());
        verify(mockInvocationBuilder).post(captor.capture());
        assertTrue(captor.getValue().getEntity() instanceof JiraCreateIssueRequestBean);
    }

    @Test
    public void testCloseIssue() throws Exception {
        when(mockResponse.getStatus()).thenReturn(204);
        ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);

        sut.closeIssue("key", auth);
        checkCapturedUrl();
        verify(mockClient).target(argThat(org.hamcrest.Matchers.endsWith("rest/api/2/issue/key/transitions/")));
        verify(mockInvocationBuilder).post(captor.capture());
        assertTrue(captor.getValue().getEntity() instanceof JiraCloseIssueRequestBean);
    }

    @Test
    public void testAddComment() throws Exception {
        when(mockResponse.getStatus()).thenReturn(201);
        ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);

        sut.addComment("key", "myComment", auth);
        checkCapturedUrl();
        verify(mockClient).target(argThat(org.hamcrest.Matchers.endsWith("rest/api/2/issue/key/comment/")));
        verify(mockInvocationBuilder).post(captor.capture());
        assertTrue(captor.getValue().getEntity() instanceof JiraAddCommentRequestBean);
    }

    @Test
    public void testGetIssue() throws Exception {
        when(mockResponse.getStatus()).thenReturn(200);
        JiraIssueResponseBean expectedIssue = JiraIssueResponseBuilder.buildDummy("key", "name", "assignee", 0);
        when(mockResponse.readEntity(JiraIssueResponseBean.class)).thenReturn(expectedIssue);

        final JiraIssue actualIssue = sut.getIssue("key", auth);
        checkCapturedUrl();
        verify(mockClient).target(argThat(Matchers.endsWith("rest/api/2/issue/key/")));

        assertNotNull(actualIssue);
        assertEquals(actualIssue.getKey(), expectedIssue.getKey());
    }

    @Test
    public void testGetComments() throws Exception {
        when(mockResponse.getStatus()).thenReturn(200);

        JiraIssueCommentsResponseBean expectedIssueWithComments = JiraIssueCommentsResponseBeanTest.createJiraIssueCommentsResponseBean();
        when(mockResponse.readEntity(JiraIssueCommentsResponseBean.class)).thenReturn(expectedIssueWithComments);

        final Collection<JiraComment> actualComments = sut.getComments("key", auth);
        checkCapturedUrl();
        verify(mockClient).target(argThat(Matchers.endsWith("rest/api/2/issue/key")));
        verify(mockInvocationBuilder).get();

        assertEquals(expectedIssueWithComments.getComments(), actualComments);
    }

    private @NotNull JqlResponseBean prepareJqlResponse(@NotNull Set<String> expectedIssueKeys, @NotNull String assigneeSuffix) {
        final JqlResponseBean response = new JqlResponseBean();
        List<JiraIssueResponseBean> issueResponses = new ArrayList<>();

        expectedIssueKeys.forEach(key -> issueResponses.add(JiraIssueResponseBuilder.buildDummy(key, "", key + assigneeSuffix, 2)));
        response.setIssues(issueResponses.toArray(new JiraIssueResponseBean[issueResponses.size()]));

        return response;
    }


}