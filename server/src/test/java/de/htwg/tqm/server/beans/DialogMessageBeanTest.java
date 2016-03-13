package de.htwg.tqm.server.beans;

public class DialogMessageBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        return new DialogMessageBean("author", 5, "body");
    }
}