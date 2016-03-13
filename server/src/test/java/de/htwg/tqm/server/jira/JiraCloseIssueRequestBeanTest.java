package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.AbstractBeanTest;

public class JiraCloseIssueRequestBeanTest extends AbstractBeanTest {

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
        String path = getClass().getResource("/close-issue-request.json").getPath();
        return readFile(path);
    }

    @Override
    protected Object createSut() {
        return new JiraCloseIssueRequestBean(2, "Done");
    }
}