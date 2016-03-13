package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.AbstractBeanTest;

public class JiraCreateIssueRequestBeanTest extends AbstractBeanTest {

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
        String path = getClass().getResource("/create-issue-request.json").getPath();
        return readFile(path);
    }

    @Override
    protected Object createSut() {
        return new JiraCreateIssueRequestBean("AUMEWT", "sikessle", "Dialog between {initiator} and {affected}",
                "This dialog is to communicate between [~sikessle] and [~sikessle].\nPlease use the comments option below.", "Task");
    }
}