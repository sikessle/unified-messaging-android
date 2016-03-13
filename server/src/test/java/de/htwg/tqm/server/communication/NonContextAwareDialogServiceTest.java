package de.htwg.tqm.server.communication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.*;
import de.htwg.tqm.server.persistence.PersistenceService;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NonContextAwareDialogServiceTest {

    private NonContextAwareDialogService sut;
    private Client initiator;
    private Client affected;
    private DialogHandler mockDialogHandler;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String HANDLER_IDENT = "MYHANDLER";
    private static final String HANDLER_DIALOG_KEY = "jira-issue1";
    private static final String NEXT_FREE_KEY = "2";
    private static final long VIOLATION_ID = 5L;
    private PersistenceService.Collection mockCollection;

    @Before
    public void setUp() throws Exception {
        PersistenceService mockPersistenceService = mock(PersistenceService.class);
        mockDialogHandler = mock(DialogHandler.class);
        when(mockDialogHandler.getUniqueHandlerIdentifier()).thenReturn(HANDLER_IDENT);
        initiator = new ClientBean("initiator", "project1", Client.Role.SM);
        affected = new ClientBean("affected", "project1", Client.Role.DEV);
        mockCollection = mock(PersistenceService.Collection.class);
        when(mockPersistenceService.getCollection(anyString())).thenReturn(mockCollection);
        when(mockDialogHandler.createDialog(any(), any(), any())).thenReturn(HANDLER_DIALOG_KEY);
        SortedSet<String> keySet = new TreeSet<>();
        keySet.add("1");
        when(mockCollection.loadKeys()).thenReturn(keySet);
        sut = new NonContextAwareDialogService(mockPersistenceService, mockDialogHandler);
    }

    @Test
    public void testCreateDialog() throws Exception {
        String subject = "sub";
        sut.createDialog(subject, VIOLATION_ID, initiator, affected);
        final ObjectNode dialogInfo = mapper.createObjectNode();
        dialogInfo.put(NonContextAwareDialogService.KEY_DIALOG_HANDLER_IDENT, HANDLER_IDENT);
        dialogInfo.put(HANDLER_IDENT, HANDLER_DIALOG_KEY);
        dialogInfo.put(NonContextAwareDialogService.KEY_SUBJECT, subject);
        dialogInfo.put(NonContextAwareDialogService.KEY_VIOLATION_ID, VIOLATION_ID);
        dialogInfo.put(NonContextAwareDialogService.KEY_INITIATOR, initiator.getName());
        dialogInfo.put(NonContextAwareDialogService.KEY_AFFECTED, affected.getName());
        dialogInfo.put(NonContextAwareDialogService.KEY_AFFECTED_RESOLVED, false);
        dialogInfo.put(NonContextAwareDialogService.KEY_INITIATOR_RESOLVED, false);
        verify(mockDialogHandler).createDialog(eq(subject), eq(initiator), eq(affected));
        verify(mockCollection).store(NEXT_FREE_KEY, dialogInfo);
    }

    @Test
    public void testCreateDialogOnEmptyDB() throws Exception {
        reset(mockCollection);
        when(mockCollection.loadKeys()).thenReturn(Collections.emptySortedSet());
        final long dialogID = sut.createDialog("subject", VIOLATION_ID, initiator, affected);
        assertThat(dialogID, equalTo(0L));
    }

    @Test
    public void testGetDialog() throws Exception {
        DialogMessage expectedMessage = new DialogMessageBean("author", 0, "body");
        SortedSet<DialogMessage> messages = new TreeSet<>();
        messages.add(expectedMessage);
        when(mockDialogHandler.getDialogMessages(HANDLER_DIALOG_KEY)).thenReturn(messages);
        long dialogID = sut.createDialog("subject", VIOLATION_ID, initiator, affected);
        final ObjectNode dialogInfo = mapper.createObjectNode();
        dialogInfo.put(NonContextAwareDialogService.KEY_DIALOG_HANDLER_IDENT, HANDLER_IDENT);
        dialogInfo.put(HANDLER_IDENT, HANDLER_DIALOG_KEY);
        dialogInfo.put(NonContextAwareDialogService.KEY_VIOLATION_ID, VIOLATION_ID);
        dialogInfo.put(NonContextAwareDialogService.KEY_INITIATOR, initiator.getName());
        dialogInfo.put(NonContextAwareDialogService.KEY_AFFECTED, affected.getName());
        dialogInfo.put(NonContextAwareDialogService.KEY_INITIATOR_RESOLVED, false);
        dialogInfo.put(NonContextAwareDialogService.KEY_AFFECTED_RESOLVED, false);
        dialogInfo.put(NonContextAwareDialogService.KEY_VIOLATION_ID, 1);
        String subject = "sub";
        dialogInfo.put(NonContextAwareDialogService.KEY_SUBJECT, subject);
        when(mockCollection.load(String.valueOf(dialogID))).thenReturn(dialogInfo);
        final Dialog actualDialog = sut.getDialog(dialogID);
        assertNotNull(actualDialog);
        assertThat(actualDialog.getSubject(), equalTo(subject));
        verify(mockDialogHandler).getDialogMessages(HANDLER_DIALOG_KEY);
        assertThat(actualDialog.getMessages().iterator().next(), equalTo(expectedMessage));
    }

    @Test
    public void testGetDialogWrongHandler() throws Exception {
        long dialogID = sut.createDialog("subject", VIOLATION_ID, initiator, affected);
        final ObjectNode dialogInfo = mapper.createObjectNode();
        dialogInfo.put(NonContextAwareDialogService.KEY_DIALOG_HANDLER_IDENT, HANDLER_IDENT + "wrongHandler");
        dialogInfo.put(HANDLER_IDENT, HANDLER_DIALOG_KEY);
        when(mockCollection.load(String.valueOf(dialogID))).thenReturn(dialogInfo);
        final Dialog actualDialog = sut.getDialog(dialogID);
        assertThat(actualDialog, equalTo(null));
    }

    @Test
    public void testMarkDialogAsResolved() throws Exception {
        String subject = "sub";
        final long dialogID = sut.createDialog(subject, VIOLATION_ID, initiator, affected);
        final ObjectNode dialogInfo = mapper.createObjectNode();
        dialogInfo.put(NonContextAwareDialogService.KEY_DIALOG_HANDLER_IDENT, HANDLER_IDENT);
        dialogInfo.put(HANDLER_IDENT, HANDLER_DIALOG_KEY);
        dialogInfo.put(NonContextAwareDialogService.KEY_VIOLATION_ID, VIOLATION_ID);
        dialogInfo.put(NonContextAwareDialogService.KEY_INITIATOR, initiator.getName());
        dialogInfo.put(NonContextAwareDialogService.KEY_AFFECTED, affected.getName());
        dialogInfo.put(NonContextAwareDialogService.KEY_INITIATOR_RESOLVED, false);
        dialogInfo.put(NonContextAwareDialogService.KEY_AFFECTED_RESOLVED, false);

        when(mockCollection.load(String.valueOf(dialogID))).thenReturn(dialogInfo);

        sut.markDialogAsResolved(dialogID, initiator);

        assertTrue(dialogInfo.get(NonContextAwareDialogService.KEY_INITIATOR_RESOLVED).asBoolean());
        assertFalse(dialogInfo.get(NonContextAwareDialogService.KEY_AFFECTED_RESOLVED).asBoolean());

        dialogInfo.put(NonContextAwareDialogService.KEY_INITIATOR_RESOLVED, true);
        verify(mockCollection, atLeastOnce()).store(eq(String.valueOf(dialogID)), eq(dialogInfo));

        sut.markDialogAsResolved(dialogID, affected);

        assertTrue(dialogInfo.get(NonContextAwareDialogService.KEY_AFFECTED_RESOLVED).asBoolean());

        dialogInfo.put(NonContextAwareDialogService.KEY_AFFECTED_RESOLVED, true);
        verify(mockCollection, atLeastOnce()).store(eq(String.valueOf(dialogID)), eq(dialogInfo));

        verify(mockDialogHandler).onMarkDialogAsResolved(eq(HANDLER_DIALOG_KEY));
    }

    @Test
    public void testGetDialogNotDialogInfo() throws Exception {
        // No dialog existing
        assertThat(sut.getDialog(1), equalTo(null));
    }

    @Test
    public void testIsDialogExistingForViolationID() throws Exception {
        Collection<JsonNode> dialogInfos = new ArrayList<>();
        dialogInfos.add(mapper.createObjectNode().put(NonContextAwareDialogService.KEY_VIOLATION_ID, VIOLATION_ID));
        when(mockCollection.loadValues()).thenReturn(dialogInfos);
        assertTrue(sut.isDialogExistingForViolationID(VIOLATION_ID));
    }

    @Test
    public void testAddMessage() throws Exception {
        DialogMessage expectedMessage = new DialogMessageBean("user", Instant.now().toEpochMilli(), "body");
        long dialogID = sut.createDialog("subject", VIOLATION_ID, initiator, affected);
        final ObjectNode dialogInfo = mapper.createObjectNode();
        dialogInfo.put(NonContextAwareDialogService.KEY_DIALOG_HANDLER_IDENT, HANDLER_IDENT);
        dialogInfo.put(NonContextAwareDialogService.KEY_VIOLATION_ID, VIOLATION_ID);
        dialogInfo.put(HANDLER_IDENT, HANDLER_DIALOG_KEY);
        when(mockCollection.load(String.valueOf(dialogID))).thenReturn(dialogInfo);

        sut.addMessage(dialogID, expectedMessage);
        verify(mockDialogHandler).addMessage(HANDLER_DIALOG_KEY, expectedMessage);
    }

    @Test
    public void testAddMessageNonExistingDialog() throws Exception {
        DialogMessage expectedMessage = new DialogMessageBean("user", Instant.now().toEpochMilli(), "body");
        sut.addMessage(-1, expectedMessage);
        verify(mockDialogHandler, never()).addMessage(anyString(), any());
    }
}