package de.htwg.tqm.server.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
final class JqlResponseBean {

    private JiraIssueResponseBean[] issues;

    public JiraIssueResponseBean[] getIssues() {
        return issues;
    }

    public void setIssues(JiraIssueResponseBean[] issues) {
        this.issues = issues;
    }

    @Override
    public String toString() {
        return "JqlResponseBean{" +
                "issues=" + Arrays.toString(issues) +
                '}';
    }
}
