package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/*
 * JiraUser model class. Represents a developer with his jira key, full name and issues
 * that are assigned to him.
 */
public class JiraUser {

    @SerializedName("name")
	private String name;

    @SerializedName("assignedIssues")
	private List<JiraIssue> assignedIssues;

    @SerializedName("assignedIssuesCount")
    private int assignedIssuesCount;

    @SerializedName("assignedIssuesCountMetricCategory")
    private MetricCategory assignedIssuesCountMetricCategory;

    public String getName() {
        return this.name;
    }

    public List<JiraIssue> getAssignedIssues() {
        return this.assignedIssues;
    }

    public int getAssignedIssuesCount() {
        return this.assignedIssuesCount;
    }

    public MetricCategory getAssignedIssuesCountMetricCategory() {
        return this.assignedIssuesCountMetricCategory;
    }
}
