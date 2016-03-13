package de.htwg.tqm.server.communication;

import de.htwg.tqm.server.beans.DialogBean;
import de.htwg.tqm.server.beans.DialogMessageBean;
import de.htwg.tqm.server.beans.Dialog;
import de.htwg.tqm.server.beans.DialogMessage;
import de.htwg.tqm.server.communication.DialogService;
import de.htwg.tqm.server.communication.SimpleDialogResponseWatcherService;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleDialogResponseWatcherServiceTest {

    private DialogService mockDialogService;

    @Before
    public void setUp() throws Exception {
        mockDialogService = mock(DialogService.class);
    }

    @Test
    public void testMissingDialog() throws Exception {
        SimpleDialogResponseWatcherService sut = new SimpleDialogResponseWatcherService(mockDialogService, Duration.ZERO);
        when(mockDialogService.getDialog(anyLong())).thenReturn(null);
        sut.watchDialog(1L);

        assertEquals(0, sut.getMissingResponses().size());
    }

    @Test
    public void testResolvedDialog() throws Exception {
        SimpleDialogResponseWatcherService sut = new SimpleDialogResponseWatcherService(mockDialogService, Duration.ZERO);
        SortedSet<DialogMessage> messages = createSingleDialogMessage("author", 0L);
        Dialog resolvedDialog = createDialog(messages, true, true, 0L);
        when(mockDialogService.getDialog(anyLong())).thenReturn(resolvedDialog);
        sut.watchDialog(1L);

        assertEquals(0, sut.getMissingResponses().size());
    }

    @Test
    public void testDialogWithoutMessagesStillTimeLeft() throws Exception {
        SimpleDialogResponseWatcherService sut = new SimpleDialogResponseWatcherService(mockDialogService, Duration.ofMinutes(5));
        Dialog emptyDialog = createDialog(new TreeSet<>(), true, false, Instant.now().toEpochMilli());
        when(mockDialogService.getDialog(anyLong())).thenReturn(emptyDialog);
        sut.watchDialog(1L);

        assertEquals(0, sut.getMissingResponses().size());
    }

    @Test
    public void testDialogWithoutMessagesTimePassed() throws Exception {
        SimpleDialogResponseWatcherService sut = new SimpleDialogResponseWatcherService(mockDialogService, Duration.ofMinutes(5));
        long timestampInPast = Instant.now().minusSeconds(60 * 60 * 60).toEpochMilli();
        Dialog emptyDialog = createDialog(new TreeSet<>(), false, true, timestampInPast);
        when(mockDialogService.getDialog(anyLong())).thenReturn(emptyDialog);
        sut.watchDialog(1L);

        assertEquals(1, sut.getMissingResponses().size());
    }

    @Test
    public void testDialogStillTimeLeft() throws Exception {
        SimpleDialogResponseWatcherService sut = new SimpleDialogResponseWatcherService(mockDialogService, Duration.ofMinutes(5));
        SortedSet<DialogMessage> messages = createSingleDialogMessage("author", Instant.now().toEpochMilli());
        Dialog dialog = createDialog(messages, true, false, Instant.now().toEpochMilli());
        when(mockDialogService.getDialog(anyLong())).thenReturn(dialog);
        sut.watchDialog(1L);

        assertEquals(0, sut.getMissingResponses().size());
    }

    @Test
    public void testDialogTimePassedInitiatorDidNotRespond() throws Exception {
        testDialogTimePassedPersonNotResponded("affected", "initiator");
    }

    @Test
    public void testDialogTimePassedAffectedDidNotRespond() throws Exception {
        testDialogTimePassedPersonNotResponded("initiator", "affected");
    }

    private void testDialogTimePassedPersonNotResponded(@NotNull String authorOfLastMessage, @NotNull String otherDialogPerson) {
        SimpleDialogResponseWatcherService sut = new SimpleDialogResponseWatcherService(mockDialogService, Duration.ofMinutes(5));
        long timestampInPast = Instant.now().minusSeconds(60 * 60 * 60).toEpochMilli();
        SortedSet<DialogMessage> messages = createSingleDialogMessage(authorOfLastMessage, timestampInPast);
        Dialog dialog = createDialog(messages, true, false, Instant.now().toEpochMilli());
        when(mockDialogService.getDialog(anyLong())).thenReturn(dialog);
        sut.watchDialog(1L);

        assertEquals(1, sut.getMissingResponses().size());
        assertEquals(otherDialogPerson, sut.getMissingResponses().iterator().next().getUserWhoDidNotRespond());
    }

    private @NotNull SortedSet<DialogMessage> createSingleDialogMessage(@NotNull String author, long timestamp) {
        SortedSet<DialogMessage> messages = new TreeSet<>();
        messages.add(new DialogMessageBean(author, timestamp, "body"));
        return  messages;
    }

    private @NotNull Dialog createDialog(@NotNull SortedSet<DialogMessage> messages, boolean resolvedInitiator, boolean resolvedAffected, long timestamp) {
        return new DialogBean(1L, "subject", 5L, messages, "initiator", "affected", resolvedInitiator, resolvedAffected, timestamp);
    }
}