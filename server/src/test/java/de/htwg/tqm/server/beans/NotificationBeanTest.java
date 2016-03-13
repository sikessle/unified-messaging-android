package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;

public class NotificationBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        Client receiver = new ClientBean("name", "project", Client.Role.DEV);
        ObjectNode content = mapper.createObjectNode();
        return new NotificationBean(Notification.Type.metricViolation, Instant.now().toEpochMilli(), receiver, content);
    }
}