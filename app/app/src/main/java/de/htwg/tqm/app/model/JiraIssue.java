package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Jira issues resource with some, not all, properties. This class
 * will be instantiated by GSON and filled via reflection.
 */
public final class JiraIssue {

    @SerializedName("key")
	private String key;

    @SerializedName("name")
	private String name;

    @SerializedName("assignee")
    private String assignee;

    @SerializedName("hoursPerUpdate")
    private double hoursPerUpdate;

    @SerializedName("updateRateMetricCategory")
	private MetricCategory updateRateMetricCategory;

    @SerializedName("link")
    private String link;

	public String getKey() {
		return key;
	}

    public String getName() {
        return this.name;
    }

    public String getLink() { return this.link; }

	public String getAssignee() {
		return this.assignee;
	}

	public double getHoursPerUpdate() {
        return this.hoursPerUpdate;
    }

    public MetricCategory getUpdateRateMetricCategory() {
        return this.updateRateMetricCategory;
    }
}