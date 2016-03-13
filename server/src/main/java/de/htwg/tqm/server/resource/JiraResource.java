package de.htwg.tqm.server.resource;

import de.htwg.tqm.server.beans.JiraIssue;
import de.htwg.tqm.server.beans.JiraProject;
import de.htwg.tqm.server.jira.JiraService;
import de.htwg.tqm.server.beans.JiraUser;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.http.HTTPException;
import java.util.Collection;

@Path("tqm/rest/metrics/jira/")
public final class JiraResource {

    @SuppressWarnings("unused")
    @Inject
    private JiraService jiraService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("projects")
    public Response getProjects(@HeaderParam("Authorization") String authHeader) {
        try {
            final Collection<JiraProject> projects = jiraService.getProjects(() -> authHeader);
            return Response.ok(projects.toArray(new JiraProject[projects.size()])).build();
        } catch (HTTPException e) {
            return Response.status(e.getStatusCode()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("projects/{projectKey}/issues")
    public Response getIssues(@PathParam("projectKey") String projectKey, @HeaderParam("Authorization") String authHeader) {
        try {
            final Collection<JiraIssue> issues = jiraService.getIssues(projectKey, () -> authHeader);
            return Response.ok(issues.toArray(new JiraIssue[issues.size()])).build();
        } catch (HTTPException e) {
            return Response.status(e.getStatusCode()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("projects/{projectKey}/users")
    public Response getUsers(@PathParam("projectKey") String projectKey, @HeaderParam("Authorization") String authHeader) {
        try {
            final Collection<JiraUser> users = jiraService.getUsers(projectKey, () -> authHeader);
            return Response.ok(users.toArray(new JiraUser[users.size()])).build();
        } catch (HTTPException e) {
            return Response.status(e.getStatusCode()).build();
        }
    }
}
