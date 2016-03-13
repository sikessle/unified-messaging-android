package de.htwg.tqm.server.poll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.ClientBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.jira.JiraService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.*;

public class CriticalMetricsTimerTaskTest {

    private CriticalMetricsTimerTask sut;
    private MetricNotificationSender mockMetricsViolationSender;
    private Client client;
    private ObjectNode content;

    @Before
    public void setUp() throws Exception {
        JiraService mockJiraService = mock(JiraService.class);
        ClientService mockClientService = mock(ClientService.class);
        client = new ClientBean("name", "project", Client.Role.DEV);
        content = new ObjectMapper().createObjectNode();

        when(mockClientService.getClient(any())).thenReturn(client);
        mockMetricsViolationSender = mock(MetricNotificationSender.class);

        sut = new CriticalMetricsTimerTask(mockJiraService, mockClientService, mockMetricsViolationSender, () -> "user:pass");
    }

    @Test
    public void testCallMetricsViolationSender() throws Exception {
        Collection<MetricViolation> violations = new ArrayList<>();
        violations.add(new MetricViolation("a metric violation name", client.getName(), content));

        sut.sendMetricViolations(violations);
        verify(mockMetricsViolationSender, atLeastOnce()).sendChecked(any(), any());
    }
}