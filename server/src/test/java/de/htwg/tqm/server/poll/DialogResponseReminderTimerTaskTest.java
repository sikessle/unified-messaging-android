package de.htwg.tqm.server.poll;

import de.htwg.tqm.server.beans.ClientBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.beans.MissingResponse;
import de.htwg.tqm.server.beans.MissingResponseBean;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.communication.DialogResponseWatcherService;
import de.htwg.tqm.server.push.PushService;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class DialogResponseReminderTimerTaskTest {

    private DialogResponseReminderTimerTask sut;
    private PushService mockPushService;

    @Before
    public void setUp() throws Exception {
        DialogResponseWatcherService mockResponseWatcher= mock(DialogResponseWatcherService.class);
        mockPushService = mock(PushService.class);
        ClientService mockClientService = mock(ClientService.class);
        sut = new DialogResponseReminderTimerTask(mockResponseWatcher, mockPushService, mockClientService);
        MissingResponse missingResponse = new MissingResponseBean(1L, "user");
        Collection<MissingResponse> missingResponses = Collections.singleton(missingResponse);

        when(mockClientService.getClient(anyString())).thenReturn(new ClientBean("", "", Client.Role.DEV));
        when(mockResponseWatcher.getMissingResponses()).thenReturn(missingResponses);
    }


    @Test
    public void testRun() throws Exception {
        sut.run();
        verify(mockPushService).send(any());
    }
}