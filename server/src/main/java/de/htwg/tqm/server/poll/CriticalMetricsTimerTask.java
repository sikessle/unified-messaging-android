package de.htwg.tqm.server.poll;

import de.htwg.tqm.server.jira.Authentication;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.jira.JiraService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import javax.xml.ws.http.HTTPException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

final class CriticalMetricsTimerTask extends TimerTask {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(CriticalMetricsTimerTask.class);

    private final ClientService clientService;
    private final JiraHoursPerUpdateChecker jiraHoursPerUpdateChecker;
    private final JiraAssignedIssuesCountChecker jiraAssignedIssuesCountChecker;
    private final MetricNotificationSender metricNotificationSender;


    public CriticalMetricsTimerTask(@NotNull JiraService jiraService,
                                    @NotNull ClientService clientService,
                                    @NotNull MetricNotificationSender metricNotificationSender,
                                    @NotNull Authentication auth) {
        this.clientService = clientService;
        this.metricNotificationSender = metricNotificationSender;
        jiraHoursPerUpdateChecker = new JiraHoursPerUpdateChecker(jiraService, auth);
        jiraAssignedIssuesCountChecker = new JiraAssignedIssuesCountChecker(jiraService, auth);
    }

    @Override
    public void run() {
        final Map<String, String> clientNameToProject = getClientNamesToProjectsMap();
        try {
            LOG.debug("Checking for metric violations..");
            checkHoursPerUpdate(clientNameToProject);
            checkAssignedIssuesCount(clientNameToProject);
        } catch (HTTPException e) {
            e.printStackTrace();
        }
    }

    private @NotNull Map<String, String> getClientNamesToProjectsMap() {
        Map<String, String> mapping = new HashMap<>();
        clientService.getRegisteredClients().forEach(client -> mapping.put(client.getName(), client.getProject()));
        return mapping;
    }

    private void checkHoursPerUpdate(@NotNull Map<String, String> clientNameToProject) {
        final Collection<MetricViolation> violations = jiraHoursPerUpdateChecker.check(clientNameToProject);
        sendMetricViolations(violations);
    }

    private void checkAssignedIssuesCount(@NotNull Map<String, String> clientNameToProject) {
        final Collection<MetricViolation> violations = jiraAssignedIssuesCountChecker.check(clientNameToProject);
        sendMetricViolations(violations);
    }

    /**
     * Checks if a message was already sent or not and if the receiver is allowed to receive the message.
     */
    void sendMetricViolations(@NotNull Collection<MetricViolation> violations) {
        LOG.debug("Found {} metric violations.", violations.size());
        violations.forEach(violation -> {
            Client causer = clientService.getClient(violation.getCauser());
            LOG.debug("Violation found for: {}", violation.getCauser());
            if (causer != null) {
                LOG.debug("Registered client found for: {}", violation.getCauser());
                metricNotificationSender.sendChecked(violation, causer);
            }
        });
    }

}
