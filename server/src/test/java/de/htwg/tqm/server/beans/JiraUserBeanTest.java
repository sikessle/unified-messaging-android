package de.htwg.tqm.server.beans;

import de.htwg.tqm.server.metric.MetricsService;

import java.util.Collection;
import java.util.Collections;

public class JiraUserBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        JiraIssue issue = new JiraIssueBean("key", "name", "asignee", 1.0, MetricsService.Category.OK.toString(), "link");
        Collection<JiraIssue> assignedIssues = Collections.singleton(issue);
        return new JiraUserBean("name", assignedIssues, MetricsService.Category.OK.toString());
    }
}