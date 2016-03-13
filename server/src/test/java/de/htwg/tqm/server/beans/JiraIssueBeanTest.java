package de.htwg.tqm.server.beans;

import de.htwg.tqm.server.metric.MetricsService;

public class JiraIssueBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        return new JiraIssueBean("key", "name", "assignee", 2.0, MetricsService.Category.OK.toString(), "link");
    }
}