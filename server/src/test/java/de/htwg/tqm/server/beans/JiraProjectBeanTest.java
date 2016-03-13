package de.htwg.tqm.server.beans;

public class JiraProjectBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        return new JiraProjectBean("key", "name");
    }
}