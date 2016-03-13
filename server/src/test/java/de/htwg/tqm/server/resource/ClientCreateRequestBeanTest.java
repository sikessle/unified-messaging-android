package de.htwg.tqm.server.resource;

import de.htwg.tqm.server.beans.AbstractBeanTest;
import de.htwg.tqm.server.beans.Client;

public class ClientCreateRequestBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        return new ClientCreateRequestBean("name", "project", Client.Role.DEV.toString());
    }
}