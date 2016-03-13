package de.htwg.tqm.server.poll;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.htwg.tqm.server.beans.*;
import de.htwg.tqm.server.communication.DialogCreationWatcherService;
import de.htwg.tqm.server.push.PushService;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DialogCreationReminderTimerTaskTest {

    private DialogCreationReminderTimerTask sut;
    private final ObjectMapper mapper = new ObjectMapper();
    private PushService mockPushService;

    @Before
    public void setUp() throws Exception {
        DialogCreationWatcherService mockCreationWatcher = mock(DialogCreationWatcherService.class);
        mockPushService = mock(PushService.class);
        sut = new DialogCreationReminderTimerTask(mockCreationWatcher, mockPushService);
        MissingDialog missingDialog = new MissingDialogBean(createNotification());
        Collection<MissingDialog> missingDialogs = Collections.singleton(missingDialog);
        when(mockCreationWatcher.getMissingDialogs()).thenReturn(missingDialogs);
    }

    private @NotNull Notification createNotification() {
        Client receiver = new ClientBean("name", "project", Client.Role.SM);
        return new NotificationBean(Notification.Type.metricViolation, 1L, receiver, mapper.createObjectNode());
    }

    @Test
    public void testRun() throws Exception {
        sut.run();
        verify(mockPushService).send(any());
    }
}