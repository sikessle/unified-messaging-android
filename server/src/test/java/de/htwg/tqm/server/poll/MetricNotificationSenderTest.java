package de.htwg.tqm.server.poll;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.ClientBean;
import de.htwg.tqm.server.beans.NotificationBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.client.InMemoryClientService;
import de.htwg.tqm.server.communication.DialogCreationWatcherService;
import de.htwg.tqm.server.persistence.PersistenceService;
import de.htwg.tqm.server.beans.Notification;
import de.htwg.tqm.server.push.PushService;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;

import static de.htwg.tqm.server.poll.MetricNotificationSender.KEY_LAST_NOTIFICATION_LONG;
import static de.htwg.tqm.server.poll.MetricNotificationSender.KEY_SENT_TO_SCRUM_MASTER_BOOL;
import static de.htwg.tqm.server.poll.MetricNotificationSender.KEY_VIOLATION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class MetricNotificationSenderTest {

    private static final long LAST_NOTIFICATION_NUMBER = 1L;
    private PushService mockPushService;
    private ClientService spyClientService;
    private MetricNotificationSender sut;
    private PersistenceService.Collection mockCollection;
    private final ObjectMapper mapper = new ObjectMapper();
    private MetricViolation violation;
    private Notification expectedNotification;
    private DialogCreationWatcherService mockCreationWatcher;

    @Before
    public void setUp() throws Exception {
        PersistenceService mockPersistenceService = mock(PersistenceService.class);
        mockCollection = mock(PersistenceService.Collection.class);
        when(mockPersistenceService.getCollection(anyString())).thenReturn(mockCollection);
        spyClientService = spy(new InMemoryClientService());
        mockPushService = mock(PushService.class);
        mockCreationWatcher = mock(DialogCreationWatcherService.class);
        sut = new MetricNotificationSender(mockPersistenceService, mockPushService, spyClientService, mockCreationWatcher, Duration.ZERO);
        JsonNode history = mapper.createObjectNode().put(MetricNotificationSender.KEY_LAST_VIOLATION_ID_LONG, LAST_NOTIFICATION_NUMBER);
        when(mockCollection.load(MetricNotificationSender.KEY_VIOLATION_IDS_HISTORY_OBJECT)).thenReturn(history);
    }

    @Test
    public void testFirstTimeNotification() throws Exception {
        when(mockCollection.containsKey(anyString())).thenReturn(false);

        Client causer = createClient(Client.Role.DEV);
        MetricViolation violation = createMetricViolation(causer);
        Notification expectedNotification = new NotificationBean(Notification.Type.metricViolation, Instant.now().toEpochMilli(),
                causer, violation.getNotificationContent());
        ArgumentCaptor<JsonNode> jsonCaptor = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);

        sut.sendChecked(violation, causer);

        verify(mockPushService).send(notificationCaptor.capture());
        verify(mockCollection).store(eq(String.valueOf(violation.hashCode())), jsonCaptor.capture());

        JsonNode actualValue = jsonCaptor.getValue();
        long now = Instant.now().toEpochMilli();

        assertEquals(now, actualValue.get(KEY_LAST_NOTIFICATION_LONG).asLong(), 500);
        assertThat(actualValue.get(KEY_SENT_TO_SCRUM_MASTER_BOOL).asBoolean(), is(false));

        Notification actualNotification = notificationCaptor.getValue();

        assertEquals(expectedNotification.getType(), actualNotification.getType());
        assertEquals(expectedNotification.getReceiver(), actualNotification.getReceiver());
    }

    @Test
    public void testSendNotYetToScrumMaster() throws Exception {
        ObjectNode notificationSentInFuture = mapper.createObjectNode();
        // Last notification is in the future (this forces the SUT to skip the scrum master notification)
        notificationSentInFuture.put(KEY_LAST_NOTIFICATION_LONG, Instant.now().plusSeconds(100).toEpochMilli());
        notificationSentInFuture.put(KEY_SENT_TO_SCRUM_MASTER_BOOL, false);
        when(mockCollection.containsKey(anyString())).thenReturn(true);
        when(mockCollection.load(anyString())).thenReturn(notificationSentInFuture);

        Client causer = createClient(Client.Role.DEV);
        MetricViolation violation = createMetricViolation(causer);

        sut.sendChecked(violation, causer);

        verify(mockCollection, never()).store(anyString(), any());
        verify(mockPushService, never()).send(any());
    }

    @Test
    public void testSendToScrumMaster() throws Exception {
        prepareSendToScrumMasterInInstanceFields();
        sendToScrumMaster(violation, false);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(mockPushService).send(notificationCaptor.capture());
        verify(mockCollection).store(eq(String.valueOf(violation.hashCode())), any());
        verify(mockCreationWatcher).watchViolation(anyLong(), any());

        Notification actualNotification = notificationCaptor.getValue();
        assertEquals(expectedNotification.getType(), actualNotification.getType());
        assertEquals(expectedNotification.getReceiver(), actualNotification.getReceiver());
    }

    @Test
    public void testSendNotAgainToScrumMaster() throws Exception {
        prepareSendToScrumMasterInInstanceFields();
        sendToScrumMaster(violation, false);
        reset(mockPushService);
        // Indicate in persistence storage that the scrum master has been already notified.
        // This means the SUT must not trigger again a notification for the scrum master.
        sendToScrumMaster(violation, true);
        verify(mockPushService, never()).send(any());
    }

    @Test
    public void testNotificationContent() throws Exception {
        when(mockCollection.containsKey(anyString())).thenReturn(false);

        Client causer = createClient(Client.Role.DEV);
        MetricViolation violation = createMetricViolation(causer);
        // false because we must NOT communicate with Dev if the receives the message himself.
        ObjectNode notificationContent = createExpectedNotificationContent(violation, false, LAST_NOTIFICATION_NUMBER + 1);
        Notification expectedNotification = new NotificationBean(Notification.Type.metricViolation, Instant.now().toEpochMilli(),
                causer, notificationContent);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        sut.sendChecked(violation, causer);
        verify(mockPushService).send(notificationCaptor.capture());

        // Skip timestamp comparison!
        final Notification actualNotification = notificationCaptor.getValue();
        assertEquals(expectedNotification.getType(), actualNotification.getType());
        assertEquals(expectedNotification.getContent(), actualNotification.getContent());
        assertEquals(expectedNotification.getReceiver(), actualNotification.getReceiver());
    }

    private ObjectNode createExpectedNotificationContent(@NotNull MetricViolation violation, boolean communicateWithDeveloper, long violationID) {
        ObjectNode content = mapper.createObjectNode();
        content.put("developer", violation.getCauser());
        content.put("violationName", violation.getViolationName());
        content.put("violationID", violationID);
        content.put("communicateWithDeveloper", communicateWithDeveloper);
        return content;
    }

    private void prepareSendToScrumMasterInInstanceFields() {
        Client scrumMaster = createClient(Client.Role.SM);
        spyClientService.registerClient(scrumMaster);
        violation = createMetricViolation(scrumMaster);
        expectedNotification = new NotificationBean(Notification.Type.metricViolation, Instant.now().toEpochMilli(),
                scrumMaster, violation.getNotificationContent());
    }

    private void sendToScrumMaster(@NotNull MetricViolation violation, boolean sentBeforeToScrumMaster) {
        ObjectNode notificationSentBelowThreshold = mapper.createObjectNode();
        // Last notification is in the past to force SUT to send notification to Scrum Master
        // SUT is configured with Threshold=0
        notificationSentBelowThreshold.put(KEY_LAST_NOTIFICATION_LONG, Instant.now().minusSeconds(100).toEpochMilli());
        notificationSentBelowThreshold.put(KEY_SENT_TO_SCRUM_MASTER_BOOL, sentBeforeToScrumMaster);
        notificationSentBelowThreshold.put(KEY_VIOLATION_ID, LAST_NOTIFICATION_NUMBER);

        when(mockCollection.containsKey(anyString())).thenReturn(true);
        when(mockCollection.load(eq(String.valueOf(violation.hashCode())))).thenReturn(notificationSentBelowThreshold);

        Client scrumMaster = createClient(Client.Role.SM);
        spyClientService.registerClient(scrumMaster);

        sut.sendChecked(violation, scrumMaster);
    }


    private @NotNull Client createClient(Client.@NotNull Role role) {
        return new ClientBean("name", "project", role);
    }

    private @NotNull MetricViolation createMetricViolation(@NotNull Client causer) {
        ObjectNode content = mapper.createObjectNode();


        return new MetricViolation("violation name", causer.getName(), content);
    }
}