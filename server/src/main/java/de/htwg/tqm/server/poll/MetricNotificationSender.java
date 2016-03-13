package de.htwg.tqm.server.poll;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.NotificationBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.communication.DialogCreationWatcherService;
import de.htwg.tqm.server.persistence.PersistenceService;
import de.htwg.tqm.server.beans.Notification;
import de.htwg.tqm.server.push.PushService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

class MetricNotificationSender {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(MetricNotificationSender.class);

    private final PushService pushService;
    private final ClientService clientService;
    private final ObjectMapper mapper;

    private final Duration durationBeforeScrumMasterGetsNotified;
    private final PersistenceService.Collection collection;
    private final DialogCreationWatcherService creationWatcher;

    private static final String COLLECTION_SENT_NOTIFICATIONS = "metric-violations";

    static final String KEY_LAST_VIOLATION_ID_LONG = "lastViolationID";
    final static String KEY_VIOLATION_IDS_HISTORY_OBJECT = "violationIDHistory";
    final static String KEY_SENT_TO_SCRUM_MASTER_BOOL = "sentToScrumMaster";
    final static String KEY_LAST_NOTIFICATION_LONG = "lastNotification";
    final static String KEY_VIOLATION_ID = "violationID";


    public MetricNotificationSender(@NotNull PersistenceService persistenceService,
                                    @NotNull PushService pushService,
                                    @NotNull ClientService clientService,
                                    @NotNull DialogCreationWatcherService creationWatcher,
                                    @NotNull Duration durationBeforeScrumMasterGetsNotified) {
        this.collection = persistenceService.getCollection(COLLECTION_SENT_NOTIFICATIONS);
        this.pushService = pushService;
        this.clientService = clientService;
        this.creationWatcher = creationWatcher;
        this.durationBeforeScrumMasterGetsNotified = durationBeforeScrumMasterGetsNotified;
        mapper = new ObjectMapper();
    }

    public void sendChecked(@NotNull MetricViolation violation, @NotNull Client causer) {
        final JsonNode persistedValue = collection.load(uniqueKey(violation));

        LOG.debug("Checking sending of metric violation: {}", violation);
        LOG.debug("Unique key of violation: {}, is in db: {}", uniqueKey(violation), persistedValue);

        if (wasSentBefore(persistedValue)) {
            LOG.debug("Violation message was sent before");
            if (mustNotifyScrumMaster(persistedValue)) {
                LOG.debug("About to notify the scrum master of project: {}", causer.getProject());
                sendNotificationToScrumMaster(violation, causer.getProject());
            }
        } else {
            LOG.debug("Sending violation message to causer: {}", causer.getName());
            sendNotificationToCauser(violation, causer);
        }
    }

    private boolean wasSentBefore(@Nullable JsonNode persistedValue) {
        return persistedValue != null;
    }

    private boolean mustNotifyScrumMaster(@NotNull JsonNode persistedValue) {
        final long lastNotification = persistedValue.get(KEY_LAST_NOTIFICATION_LONG).asLong();
        final long now = Instant.now().toEpochMilli();
        final Duration durationSinceLastNotification = Duration.ofMillis(now - lastNotification);

        return notSentToScrumMastersBefore(persistedValue) && durationSinceLastNotification.compareTo(durationBeforeScrumMasterGetsNotified) >= 0;
    }

    private boolean notSentToScrumMastersBefore(@NotNull JsonNode persistedValue) {
        return !persistedValue.get(KEY_SENT_TO_SCRUM_MASTER_BOOL).asBoolean();
    }

    private void sendNotificationToScrumMaster(@NotNull MetricViolation violation, String project) {
        long violationID = getViolationID(violation);
        clientService.getRegisteredClientsFor(Client.Role.SM, project).forEach(scrumMaster -> {
            Notification notification = sendNotification(scrumMaster, violation, true, violationID);
            // Watch if the person does create a dialog
            creationWatcher.watchViolation(violationID, notification);
        });
        storeNotificationSentInDB(violation, true, violationID);
    }

    private void sendNotificationToCauser(@NotNull MetricViolation violation, @NotNull Client causer) {
        long violationID = getViolationID(violation);
        sendNotification(causer, violation, false, violationID);
        storeNotificationSentInDB(violation, false, violationID);
    }

    private @NotNull Notification sendNotification(@NotNull Client receiver, @NotNull MetricViolation violation, boolean communicateWithDeveloper, long violationID) {
        ObjectNode content = getEnrichedNotificationContentOfViolation(violation, communicateWithDeveloper, violationID);
        Notification notification = new NotificationBean(Notification.Type.metricViolation, Instant.now().toEpochMilli(), receiver, content);
        pushService.send(notification);
        return notification;
    }

    private void storeNotificationSentInDB(@NotNull MetricViolation violation, boolean sentToScrumMaster, long violationID) {
        ObjectNode value = mapper.createObjectNode();
        value.put(KEY_LAST_NOTIFICATION_LONG, Instant.now().toEpochMilli());
        value.put(KEY_SENT_TO_SCRUM_MASTER_BOOL, sentToScrumMaster);
        value.put(KEY_VIOLATION_ID, violationID);

        collection.store(uniqueKey(violation), value);

        LOG.debug("Stored unique key in db: {}", uniqueKey(violation));
    }

    private @NotNull ObjectNode getEnrichedNotificationContentOfViolation(@NotNull MetricViolation violation, boolean communicateWithDeveloper,
                                                                          long violationID) {
        ObjectNode content = violation.getNotificationContent().deepCopy();

        content.put("developer", violation.getCauser());
        content.put("communicateWithDeveloper", communicateWithDeveloper);
        content.put("violationName", violation.getViolationName());
        content.put("violationID", violationID);

        return content;
    }

    private long getViolationID(@NotNull MetricViolation violation) {
        final long id;

        final JsonNode sentNotificationsEntry = collection.load(uniqueKey(violation));

        if (sentNotificationsEntry != null) {
            // 1. Was sent before -> get key from db
            id = sentNotificationsEntry.get(KEY_VIOLATION_ID).asLong();
        } else {
            // 2. New violation message -> create next free key
            if (!collection.containsKey(KEY_VIOLATION_IDS_HISTORY_OBJECT)) {
                ObjectNode history = mapper.createObjectNode();
                history.put(KEY_LAST_VIOLATION_ID_LONG, -1L);
                collection.store(KEY_VIOLATION_IDS_HISTORY_OBJECT, history);
            }
            final ObjectNode nextFreeViolationIDObject = (ObjectNode) collection.load(KEY_VIOLATION_IDS_HISTORY_OBJECT);
            assert nextFreeViolationIDObject != null; // As we create it before
            id = nextFreeViolationIDObject.get(KEY_LAST_VIOLATION_ID_LONG).asLong() + 1;
            nextFreeViolationIDObject.put(KEY_LAST_VIOLATION_ID_LONG, id);
        }

        return id;

    }

    private @NotNull String uniqueKey(@NotNull MetricViolation violation) {
        return String.valueOf(violation.hashCode());
    }

}
