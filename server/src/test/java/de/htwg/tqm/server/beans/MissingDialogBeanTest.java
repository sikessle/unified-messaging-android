package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.htwg.tqm.server.beans.*;

public class MissingDialogBeanTest extends AbstractBeanTest {

    @Override
    protected Object createSut() {
        return new MissingDialogBean(new NotificationBean(Notification.Type.dialogCreated, 1L,
                new ClientBean("name", "project", Client.Role.DEV),
                new ObjectMapper().createObjectNode()));
    }

}