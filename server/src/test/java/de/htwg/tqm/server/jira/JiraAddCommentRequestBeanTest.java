package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.AbstractBeanTest;

public class JiraAddCommentRequestBeanTest extends AbstractBeanTest {

    @Override
    protected boolean doTestDeserialize() {
        return false;
    }

    @Override
    protected boolean doTestCompareJsonAfterSerialization() {
        return true;
    }

    @Override
    protected String getExpectedJsonAfterSerialization() throws Exception {
        String path = getClass().getResource("/add-comment-request.json").getPath();
        return readFile(path);
    }

    @Override
    protected Object createSut() {
        return new JiraAddCommentRequestBean("Message from: [~sikessle]\n\nHello!");
    }
}