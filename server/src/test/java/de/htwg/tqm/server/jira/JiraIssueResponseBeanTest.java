package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.AbstractBeanTest;

@SuppressWarnings("unused")
public class JiraIssueResponseBeanTest {

    public static class SearchSingleIssueResponseTest extends AbstractBeanTest {
        @Override
        protected Object createSut() {
            return JiraIssueResponseBuilder.buildDummy("AUMEFUENF-363", "Anbindung der Server-Datenquelle", "tokeh", 0);
        }

        @Override
        protected String getJsonToDeserialize() throws Exception {
            String path = getClass().getResource("/jira-search-single-issue.json").getPath();
            return readFile(path);
        }
    }

    public static class CreateIssueResponseTest extends AbstractBeanTest {
        @Override
        protected Object createSut() {
            return new JiraIssueResponseBean("key", null);
        }

        @Override
        protected String getJsonToDeserialize() throws Exception {
            String path = getClass().getResource("/create-issue-response.json").getPath();
            return readFile(path);
        }
    }

    public static class SingleIssueResponseTest extends AbstractBeanTest {
        @Override
        protected Object createSut() {
            return JiraIssueResponseBuilder.buildDummy("AUMEWT-106", "Dialog between {initiator} and {affected}", "sikessle", 0);
        }

        @Override
        protected String getJsonToDeserialize() throws Exception {
            String path = getClass().getResource("/single-issue-response.json").getPath();
            return readFile(path);
        }
    }
}