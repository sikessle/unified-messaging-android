package de.htwg.tqm.server.poll;

import de.htwg.tqm.server.beans.ClientBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.communication.DialogCreationWatcherService;
import de.htwg.tqm.server.communication.DialogResponseWatcherService;
import de.htwg.tqm.server.jira.JiraService;
import de.htwg.tqm.server.persistence.PersistenceService;
import de.htwg.tqm.server.push.PushService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;

import static org.mockito.Mockito.*;

public class ThreadedPollServiceTest {

    private ThreadedPollService sut;
    private JiraService mockJiraService;
    private static final int INTERVAL_MILLIS = 100;
    private static final int WAIT_MILLIS = 500;

    @Before
    public void setUp() throws Exception {
        PersistenceService mockPersistenceService = mock(PersistenceService.class);
        mockJiraService = mock(JiraService.class);
        PushService mockPushService = mock(PushService.class);
        ClientService mockClientService = mock(ClientService.class);
        Collection<Client> clients = new HashSet<>();
        clients.add(new ClientBean("name", "project", Client.Role.DEV));
        DialogResponseWatcherService mockDialogResponseWatcherService = mock(DialogResponseWatcherService.class);
        DialogCreationWatcherService mockCreationWatcher = mock(DialogCreationWatcherService.class);

        when(mockClientService.getRegisteredClients()).thenReturn(clients);

        sut = new ThreadedPollService(mockPersistenceService, mockJiraService, mockClientService,
                mockPushService, mockDialogResponseWatcherService, mockCreationWatcher, () -> "user:pass");
        sut.setMetricPollIntervalMillis(INTERVAL_MILLIS);
        sut.setCommunicationReminderPollIntervalMillis(INTERVAL_MILLIS);
        sut.setDurationBeforeScrumMasterGetsNotified(Duration.ZERO);
    }

    @After
    public void tearDown() throws Exception {
        sut.shutdown();
    }

    @Test
    public void testStart() throws Exception {
        sut.start();
        sleepBetweenInterval();
        verify(mockJiraService, atLeastOnce()).getIssues(anyString(), any());
    }

    @Test
    public void testShutdown() throws Exception {
        sut.start();
        sleepBetweenInterval();
        sut.shutdown();
        Mockito.reset(mockJiraService);
        sleepBetweenInterval();
        verify(mockJiraService, never()).getIssues(anyString(), any());
    }

    private void sleepBetweenInterval() throws Exception {
        Thread.sleep(INTERVAL_MILLIS + WAIT_MILLIS);
    }
}