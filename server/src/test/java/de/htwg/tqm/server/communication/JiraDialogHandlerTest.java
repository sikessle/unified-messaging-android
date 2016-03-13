package de.htwg.tqm.server.communication;

import de.htwg.tqm.server.beans.*;
import de.htwg.tqm.server.beans.JiraComment;
import de.htwg.tqm.server.jira.JiraService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class JiraDialogHandlerTest {

    private static final String DIALOG_KEY = "issue-1";
    private final Client affected = new ClientBean("developer", "project", Client.Role.DEV);
    private final Client initiator = new ClientBean("scrumMaster", "project", Client.Role.SM);
    private JiraService mockJira;
    private JiraDialogHandler sut;

    @Before
    public void setUp() throws Exception {
        mockJira = mock(JiraService.class);
        sut = new JiraDialogHandler(mockJira, () -> "Basic base64(user:pass)");
    }

    @Test
    public void testGetUniqueHandlerIdentifier() throws Exception {
        assertTrue(sut.getUniqueHandlerIdentifier().equals("JIRA"));
    }

    @Test
    public void testGetMissingDialog() throws Exception {
        assertTrue(sut.getDialogMessages("NONE").isEmpty());
    }

    @Test
    public void testGetMessages() throws Exception {
        Collection<JiraComment> expectedComments = new ArrayList<>();
        final JiraCommentBean expectedComment = new JiraCommentBean("author", 10, "content");
        expectedComments.add(expectedComment);
        when(mockJira.getComments(eq(DIALOG_KEY), any())).thenReturn(expectedComments);
        when(mockJira.getIssue(eq(DIALOG_KEY), any())).thenReturn(new JiraIssueBean(DIALOG_KEY, "", "", 1.0, "C", ""));

        final Collection<DialogMessage> messages = sut.getDialogMessages(DIALOG_KEY);
        verify(mockJira).getComments(eq(DIALOG_KEY), any());


        final DialogMessage actualMessage = messages.iterator().next();

        assertThat(actualMessage.getAuthor(), equalTo(expectedComment.getAuthor()));
        assertThat(actualMessage.getBody(), equalTo(expectedComment.getContent()));
        assertThat(actualMessage.getTimestamp(), equalTo(expectedComment.getTimestamp()));
    }

    @Test
    public void testCreateDialog() throws Exception {
        when(mockJira.createIssue(anyString(), anyString(), anyString(), anyString(), anyString(), any())).thenReturn(DIALOG_KEY);
        final String dialogKey = sut.createDialog("subject", initiator, affected);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockJira).createIssue(anyString(), captor.capture(), anyString(), eq(initiator.getName()), eq("Task"), any());
        verify(mockJira).createIssue(anyString(), anyString(), eq(initiator.getProject()), eq(initiator.getName()), eq("Task"), any());
        assertThat(dialogKey, equalTo(DIALOG_KEY));

        assertThat(captor.getValue(), containsString("[~" + initiator.getName() + "]"));
        assertThat(captor.getValue(), containsString("[~" + affected.getName() + "]"));
    }

    @Test
    public void testAddMessage() throws Exception {
        DialogMessage message = new DialogMessageBean("author", 20, "My Comment is.");
        when(mockJira.getIssue(eq(DIALOG_KEY), any())).thenReturn(new JiraIssueBean(DIALOG_KEY, "", "", 1.0, "C", ""));
        sut.addMessage(DIALOG_KEY, message);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockJira).addComment(eq(DIALOG_KEY), captor.capture(), any());
        assertThat(captor.getValue(), containsString(message.getBody()));
        assertThat(captor.getValue(), containsString("[~" + message.getAuthor() + "]"));
    }

    @Test
    public void testOnMarkDialogAsResolved() throws Exception {
        sut.onMarkDialogAsResolved(DIALOG_KEY);

        verify(mockJira).closeIssue(eq(DIALOG_KEY), any());
    }
}