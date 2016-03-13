package de.htwg.tqm.server;

import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.client.InMemoryClientService;
import de.htwg.tqm.server.communication.*;
import de.htwg.tqm.server.jira.Authentication;
import de.htwg.tqm.server.jira.JiraRestService;
import de.htwg.tqm.server.jira.JiraService;
import de.htwg.tqm.server.metric.DefaultMetricsService;
import de.htwg.tqm.server.metric.MetricsService;
import de.htwg.tqm.server.persistence.MapDBPersistenceService;
import de.htwg.tqm.server.persistence.PersistenceService;
import de.htwg.tqm.server.poll.*;
import de.htwg.tqm.server.push.PushService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Base64;

public class TqmBinder extends AbstractBinder {

    private final PushService pushService;

    /**
     * @param pushService Fields of this object must be manually injected.
     */
    public TqmBinder(@NotNull PushService pushService) {
        this.pushService = pushService;
    }

    @Override
    protected void configure() {
        bind(pushService).to(PushService.class);
        bind(ThreadedPollService.class).to(PollService.class).in(Singleton.class);
        bind(JiraRestService.class).to(JiraService.class).in(Singleton.class);
        bind(InMemoryClientService.class).to(ClientService.class).in(Singleton.class);
        bind(MapDBPersistenceService.class).to(PersistenceService.class).in(Singleton.class);
        bind(ClientBuilder.newClient()).to(Client.class);
        bind(DefaultMetricsService.class).to(MetricsService.class).in(Singleton.class);
        bind(NonContextAwareDialogService.class).to(DialogService.class).in(Singleton.class);
        bind(JiraDialogHandler.class).to(DialogHandler.class).in(Singleton.class);
        bind(SimpleDialogResponseWatcherService.class).to(DialogResponseWatcherService.class).in(Singleton.class);
        bind(SimpleDialogCreationWatcherService.class).to(DialogCreationWatcherService.class).in(Singleton.class);

        Authentication jiraServerAuth = () -> "Basic " + Base64.getEncoder().encodeToString("sikessle:sikessle".getBytes());
        bind(jiraServerAuth).to(Authentication.class).named("jiraServerAuth");
        bind("/tmp/tqm-db").to(String.class).named("dbPath");

        bind(Duration.ofSeconds(10)).to(Duration.class).named("durationBeforeScrumMasterGetsNotified");
        bind(Duration.ofSeconds(10)).to(Duration.class).named("durationBeforeRemindForDialogCreation");
        bind(Duration.ofSeconds(10)).to(Duration.class).named("durationBeforeRemindForDialogResponse");

        bind(Duration.ofSeconds(20).toMillis()).to(Long.class).named("metricPollIntervalMillis");
        bind(Duration.ofSeconds(40).toMillis()).to(Long.class).named("communicationReminderPollIntervalMillis");

        bind(1).to(Integer.class).named("assignedIssuesOK");
        bind(3).to(Integer.class).named("assignedIssuesWARN");

        bind(2.0).to(Double.class).named("hoursPerUpdateOK");
        bind(4.0).to(Double.class).named("hoursPerUpdateWARN");

        try {
            bind(new URL("http://metaproject.in.fhkn.de:8080")).to(URL.class).named("jiraBaseUrl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


}
