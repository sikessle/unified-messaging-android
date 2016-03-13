package de.htwg.tqm.server.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.htwg.tqm.server.beans.ClientBean;
import de.htwg.tqm.server.beans.NotificationBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.communication.DialogService;
import de.htwg.tqm.server.beans.Notification;
import de.htwg.tqm.server.communication.SimpleDialogCreationWatcherService;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleDialogCreationWatcherServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private DialogService mockDialogService;

    @Before
    public void setUp() throws Exception {
        mockDialogService = mock(DialogService.class);
    }


    @Test
    public void testGetMissingDialogs() throws Exception {
        SimpleDialogCreationWatcherService sut = new SimpleDialogCreationWatcherService(mockDialogService, Duration.ZERO);

        sut.watchViolation(1L, createNotification(Instant.now().toEpochMilli()));

        assertEquals(1, sut.getMissingDialogs().size());
    }

    @Test
    public void testDialogMissingButStillTimeLeft() throws Exception {
        SimpleDialogCreationWatcherService sut = new SimpleDialogCreationWatcherService(mockDialogService, Duration.ofMinutes(5));

        sut.watchViolation(1L, createNotification(Instant.now().toEpochMilli()));

        assertEquals(0, sut.getMissingDialogs().size());
    }

    @Test
    public void testDialogMissingTimeOver() throws Exception {
        SimpleDialogCreationWatcherService sut = new SimpleDialogCreationWatcherService(mockDialogService, Duration.ofMinutes(1));

        long timestampInPast = Instant.now().minusSeconds(60*60).toEpochMilli();
        sut.watchViolation(1L, createNotification(timestampInPast));

        assertEquals(1, sut.getMissingDialogs().size());
    }

    @Test
    public void testUnwatchOnDialogCreation() throws Exception {
        SimpleDialogCreationWatcherService sut = new SimpleDialogCreationWatcherService(mockDialogService, Duration.ZERO);

        sut.watchViolation(1L, createNotification(Instant.now().toEpochMilli()));
        when(mockDialogService.isDialogExistingForViolationID(anyLong())).thenReturn(true);

        assertEquals(0, sut.getMissingDialogs().size());
    }

    private @NotNull Notification createNotification(long timestamp) {
        Client receiver = new ClientBean("name", "project", Client.Role.SM);
        return new NotificationBean(Notification.Type.metricViolation, timestamp, receiver, mapper.createObjectNode());
    }
}