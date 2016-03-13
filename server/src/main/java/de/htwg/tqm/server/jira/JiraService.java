package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.JiraComment;
import de.htwg.tqm.server.beans.JiraIssue;
import de.htwg.tqm.server.beans.JiraProject;
import de.htwg.tqm.server.beans.JiraUser;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.ws.http.HTTPException;
import java.util.Collection;

@ThreadSafe
public interface JiraService {

    /**
     * Returns all issues for the given project which are in progress and assigned to any user.
     */
    @NotNull Collection<JiraIssue> getIssues(@NotNull String projectKey, @NotNull Authentication auth) throws HTTPException;

    /**
     * Returns all users of the project.
     */
    @NotNull Collection<JiraUser> getUsers(@NotNull String projectKey, @NotNull Authentication auth) throws HTTPException;

    /**
     * Returns all projects from JIRA
     */
    @NotNull Collection<JiraProject> getProjects(@NotNull Authentication auth) throws HTTPException;

    /**
     * Creates an issue.
     * @return The issueKey
     */
    @NotNull String createIssue(@NotNull String name, @NotNull String description, @NotNull String projectKey,
                                @NotNull String assignee, @NotNull String issueType, @NotNull Authentication auth) throws HTTPException;

    /**
     * Returns an issue if any is found.
     */
    @Nullable JiraIssue getIssue(@NotNull String issueKey, @NotNull Authentication auth) throws HTTPException;

    /**
     * Transitions an issue to state "closed"
     */
    void closeIssue(@NotNull String issueKey, @NotNull Authentication auth) throws HTTPException;

    /**
     * Returns all comments for an issue
     */
    @NotNull Collection<JiraComment> getComments(@NotNull String issueKey, @NotNull Authentication auth);

    /**
     * Adds a comment to an issue
     */
    void addComment(@NotNull String issueKey, @NotNull String comment, @NotNull Authentication auth) throws HTTPException;

}
