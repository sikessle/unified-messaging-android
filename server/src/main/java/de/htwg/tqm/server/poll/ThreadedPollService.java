package de.htwg.tqm.server.poll;

import de.htwg.tqm.server.communication.DialogCreationWatcherService;
import de.htwg.tqm.server.communication.DialogResponseWatcherService;
import de.htwg.tqm.server.jira.Authentication;
import de.htwg.tqm.server.persistence.PersistenceService;
import de.htwg.tqm.server.push.PushService;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.jira.JiraService;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

@Singleton
@NotThreadSafe
public final class ThreadedPollService implements PollService {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(ThreadedPollService.class);

    private final Authentication auth;
    private final ClientService clientService;
    private final DialogResponseWatcherService responseWatcher;
    private final DialogCreationWatcherService creationWatcher;
    private Duration durationBeforeScrumMasterGetsNotified = Duration.ofMinutes(10);
    private long metricPollIntervalMillis = Duration.ofMinutes(10).toMillis();
    private long communicationReminderPollIntervalMillis = Duration.ofMinutes(15).toMillis();
    private final PushService pushService;
    private final PersistenceService persistenceService;
    private final JiraService jiraService;
    private Timer timerMetrics;
    private Timer timerCommunication;

    @Inject
    public ThreadedPollService(@NotNull PersistenceService persistenceService,
                               @NotNull JiraService jiraService,
                               @NotNull ClientService clientService,
                               @NotNull PushService pushService,
                               @NotNull DialogResponseWatcherService responseWatcher,
                               @NotNull DialogCreationWatcherService creationWatcher,
                               @NotNull @Named("jiraServerAuth") Authentication auth) {
        this.persistenceService = persistenceService;
        this.jiraService = jiraService;
        this.clientService = clientService;
        this.pushService = pushService;
        this.responseWatcher = responseWatcher;
        this.creationWatcher = creationWatcher;
        this.auth = auth;
    }

    @Inject
    public void setDurationBeforeScrumMasterGetsNotified(@NotNull @Named("durationBeforeScrumMasterGetsNotified") Duration durationBeforeScrumMasterGetsNotified) {
        this.durationBeforeScrumMasterGetsNotified = durationBeforeScrumMasterGetsNotified;
    }

    @Inject
    public void setMetricPollIntervalMillis(@Named("metricPollIntervalMillis") long metricPollIntervalMillis) {
        this.metricPollIntervalMillis = metricPollIntervalMillis;
    }

    @Inject
    public void setCommunicationReminderPollIntervalMillis(@Named("communicationReminderPollIntervalMillis") long communicationReminderPollIntervalMillis) {
        this.communicationReminderPollIntervalMillis = communicationReminderPollIntervalMillis;
    }

    @Override
    public void start() {
        if (timerMetrics == null && timerCommunication == null) {
            timerMetrics = new Timer();
            MetricNotificationSender sender = new MetricNotificationSender(persistenceService, pushService, clientService, creationWatcher, durationBeforeScrumMasterGetsNotified);
            TimerTask metricsTimerTask = new CriticalMetricsTimerTask(jiraService, clientService, sender, auth);
            timerMetrics.schedule(metricsTimerTask, metricPollIntervalMillis, metricPollIntervalMillis);

            timerCommunication = new Timer();
            TimerTask dialogCreationReminderTask = new DialogCreationReminderTimerTask(creationWatcher, pushService);
            timerCommunication.schedule(dialogCreationReminderTask, communicationReminderPollIntervalMillis, communicationReminderPollIntervalMillis);
            TimerTask dialogResponseReminderTask = new DialogResponseReminderTimerTask(responseWatcher, pushService, clientService);
            timerCommunication.schedule(dialogResponseReminderTask, communicationReminderPollIntervalMillis, communicationReminderPollIntervalMillis);

            LOG.debug("Started");
        }
    }

    @Override
    public void shutdown() {
        if (timerMetrics != null && timerCommunication != null) {
            timerMetrics.cancel();
            timerMetrics.purge();
            timerMetrics = null;

            timerCommunication.cancel();
            timerCommunication.purge();
            timerCommunication = null;

            LOG.debug("Shutdown");
        }
    }
}
