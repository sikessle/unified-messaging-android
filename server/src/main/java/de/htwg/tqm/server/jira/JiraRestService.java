package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.*;
import de.htwg.tqm.server.metric.MetricsService;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.http.HTTPException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@ThreadSafe
@Singleton
public final class JiraRestService implements JiraService {

    /**
     * Resource key (URI component) to access the projects resource
     */
    private static final String PROJECTS = "project/";
    private static final String ISSUE = "issue/";
    private static final String TRANSITIONS = "transitions/";
    private static final String COMMENTS = "comment/";
    /**
     * Char as a placeholder
     */
    private static final String REPLACE_CHAR = "#";
    /**
     * Replace # with a project key.
     */
    private static final String ISSUES_TEMPLATE = "search?jql=project="
            + REPLACE_CHAR
            + "%20AND%20status=%22In%20Progress%22"
            + "%20AND%20assignee%20is%20not%20EMPTY"
            + "&fields=summary,assignee,worklog,timetracking&maxResults=900";

    private final javax.ws.rs.client.Client client;
    private final String jiraBaseUrl;
    private final JiraIssueBuilder jiraIssueBuilder;
    private final MetricsService categorizer;

    @Inject
    public JiraRestService(@NotNull javax.ws.rs.client.Client client,
                           @NotNull @Named("jiraBaseUrl") URL jiraBaseUrl,
                           @NotNull MetricsService categorizer) {
        this.client = client;
        this.categorizer = categorizer;
        jiraIssueBuilder = new JiraIssueBuilder(categorizer, jiraBaseUrl);
        try {
            this.jiraBaseUrl = new URL(jiraBaseUrl, "rest/api/2/").toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Override
    public @NotNull Collection<JiraIssue> getIssues(@NotNull String projectKey, @NotNull Authentication auth) throws HTTPException {
        String url = jiraBaseUrl + ISSUES_TEMPLATE.replace(REPLACE_CHAR, projectKey);
        final Response response = getAuthorizedBuilder(auth, url).accept(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {
            final JqlResponseBean jqlResponse = response.readEntity(JqlResponseBean.class);
            return getIssuesCleaned(jqlResponse);
        } else {
            throw new HTTPException(response.getStatus());
        }
    }

    private Collection<JiraIssue> getIssuesCleaned(JqlResponseBean jqlResponse) {
        List<JiraIssue> result = new ArrayList<>();

        for (JiraIssueResponseBean jiraIssueResponse : jqlResponse.getIssues()) {
            result.add(jiraIssueBuilder.fromIssueResponse(jiraIssueResponse));
        }

        return result;
    }

    @Override
    public @NotNull Collection<JiraUser> getUsers(@NotNull String projectKey, @NotNull Authentication auth) throws HTTPException {
        final Collection<JiraIssue> issues = getIssues(projectKey, auth);

        final Collection<JiraUser> users = new ArrayList<>();
        final Map<String, Collection<JiraIssue>> userNameToIssue = mapUserNameToIssue(issues);
        final Map<String, MetricsService.Category> userCategories = getUserCategories(userNameToIssue);

        userNameToIssue.forEach((name, assignedIssues) -> users.add(new JiraUserBean(name, assignedIssues, userCategories.get(name).toString())));

        return users;
    }

    private @NotNull Map<String, Collection<JiraIssue>> mapUserNameToIssue(@NotNull Collection<JiraIssue> issues) {
        final Map<String, Collection<JiraIssue>> mapping = new HashMap<>();

        issues.forEach(issue -> {
            if (!mapping.containsKey(issue.getAssignee())) {
                mapping.put(issue.getAssignee(), new ArrayList<>());
            }
            mapping.get(issue.getAssignee()).add(issue);
        });

        return mapping;
    }

    private @NotNull Map<String, MetricsService.Category> getUserCategories(@NotNull Map<String, Collection<JiraIssue>> userNameToIssue) {
        Map<String, MetricsService.Category> mapping = new HashMap<>();

        userNameToIssue.forEach((name, issues) -> mapping.put(name, categorizer.categoryOfUser(issues.size())));

        return mapping;
    }

    @Override
    public @NotNull Collection<JiraProject> getProjects(@NotNull Authentication auth) throws HTTPException {
        String url = jiraBaseUrl + PROJECTS;
        final Response response = getAuthorizedBuilder(auth, url).accept(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {
            final JiraProjectBean[] jiraProjects = response.readEntity(JiraProjectBean[].class);
            return Arrays.asList(jiraProjects);
        } else {
            throw new HTTPException(response.getStatus());
        }
    }

    @Override
    public @NotNull String createIssue(@NotNull String name, @NotNull String description,
                                       @NotNull String projectKey, @NotNull String assignee,
                                       @NotNull String issueType, @NotNull Authentication auth) throws HTTPException {
        String url = jiraBaseUrl + ISSUE;
        JiraCreateIssueRequestBean createIssueBean = new JiraCreateIssueRequestBean(projectKey, assignee, name, description, issueType);
        final Response response = getAuthorizedBuilder(auth, url).post(Entity.entity(createIssueBean, MediaType.APPLICATION_JSON));

        if (response.getStatus() == 201) {
            final JiraIssueResponseBean issue = response.readEntity(JiraIssueResponseBean.class);
            return jiraIssueBuilder.fromIssueResponse(issue).getKey();
        } else {
            throw new HTTPException(response.getStatus());
        }
    }

    @Override
    public void closeIssue(@NotNull String issueKey, @NotNull Authentication auth) throws HTTPException {
        String url = jiraBaseUrl + ISSUE + issueKey + "/" + TRANSITIONS;
        JiraCloseIssueRequestBean closeIssueBean = new JiraCloseIssueRequestBean(2, "Done");
        final Response response = getAuthorizedBuilder(auth, url).post(Entity.entity(closeIssueBean, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 204) {
            throw new HTTPException(response.getStatus());
        }
    }

    @Override
    public void addComment(@NotNull String issueKey, @NotNull String comment, @NotNull Authentication auth) throws HTTPException {
        String url = jiraBaseUrl + ISSUE + issueKey + "/" + COMMENTS;
        JiraAddCommentRequestBean addCommentBean = new JiraAddCommentRequestBean(comment);
        final Response response = getAuthorizedBuilder(auth, url).post(Entity.entity(addCommentBean, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 201) {
            throw new HTTPException(response.getStatus());
        }
    }


    @Override
    public @Nullable JiraIssue getIssue(@NotNull String issueKey, @NotNull Authentication auth) throws HTTPException {
        String url = jiraBaseUrl + ISSUE + issueKey + "/";
        final Response response = getAuthorizedBuilder(auth, url).accept(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {
            final JiraIssueResponseBean issueResponseBean = response.readEntity(JiraIssueResponseBean.class);
            return jiraIssueBuilder.fromIssueResponse(issueResponseBean);
        } else {
            throw new HTTPException(response.getStatus());
        }
    }

    @Override
    public @NotNull Collection<JiraComment> getComments(@NotNull String issueKey, @NotNull Authentication auth) {
        String url = jiraBaseUrl + ISSUE + issueKey;

        final Response response = getAuthorizedBuilder(auth, url).accept(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {
            final JiraIssueCommentsResponseBean issueResponseBean = response.readEntity(JiraIssueCommentsResponseBean.class);
            return issueResponseBean.getComments();
        } else {
            throw new HTTPException(response.getStatus());
        }
    }

    private Invocation.Builder getAuthorizedBuilder(@NotNull Authentication auth, @NotNull String url) {
        return client.target(url).request().header("Authorization", auth.getBasicAuthValue());
    }


}
