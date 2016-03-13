package de.htwg.tqm.server.beans;

public class ClientBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        return new ClientBean("name", "project", Client.Role.DEV);
    }
}