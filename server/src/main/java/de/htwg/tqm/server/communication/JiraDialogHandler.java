package de.htwg.tqm.server.communication;

import de.htwg.tqm.server.beans.DialogMessage;
import de.htwg.tqm.server.beans.DialogMessageBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.jira.Authentication;
import de.htwg.tqm.server.beans.JiraComment;
import de.htwg.tqm.server.beans.JiraIssue;
import de.htwg.tqm.server.jira.JiraService;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@ThreadSafe
public final class JiraDialogHandler implements DialogHandler {

    private final JiraService jiraService;
    private final Authentication jiraAuth;

    @Inject
    public JiraDialogHandler(@NotNull JiraService jiraService, @NotNull @Named("jiraServerAuth") Authentication jiraAuth) {
        this.jiraService = jiraService;
        this.jiraAuth = jiraAuth;
    }

    @Override
    public @NotNull String getUniqueHandlerIdentifier() {
        return "JIRA";
    }

    @Override
    public @NotNull String createDialog(@NotNull String subject, @NotNull Client initiator, @NotNull Client affected) {
        String description = "This dialog is to communicate between [~" + initiator.getName() + "] and [~" + affected.getName() + "]." +
                "\nPlease use the comments option below.";
        return jiraService.createIssue(subject, description, initiator.getProject(), initiator.getName(), "Task", jiraAuth);
    }

    @Override
    public @NotNull SortedSet<DialogMessage> getDialogMessages(@NotNull String dialogKey) {
        JiraIssue issue = jiraService.getIssue(dialogKey, jiraAuth);
        if (issue == null) {
            return Collections.emptySortedSet();
        }
        final Collection<JiraComment> comments = jiraService.getComments(issue.getKey(), jiraAuth);
        SortedSet<DialogMessage> messages = new TreeSet<>();
        comments.forEach(comment -> messages.add(new DialogMessageBean(comment.getAuthor(), comment.getTimestamp(), comment.getContent())));

        return messages;
    }

    @Override
    public void addMessage(@NotNull String dialogKey, @NotNull DialogMessage message) {
        final JiraIssue issue = jiraService.getIssue(dialogKey, jiraAuth);
        if (issue == null) {
            return;
        }
        String body = "Message from: [~" + message.getAuthor() + "]\n\n";
        body += message.getBody();
        jiraService.addComment(issue.getKey(), body, jiraAuth);
    }

    @Override
    public void onMarkDialogAsResolved(@NotNull String dialogKey) {
        jiraService.closeIssue(dialogKey, jiraAuth);
    }
}
