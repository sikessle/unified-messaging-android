package de.htwg.tqm.server.beans;

public class JiraCommentBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        return new JiraCommentBean("author", 2, "content");
    }
}