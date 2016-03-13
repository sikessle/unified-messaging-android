package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class JiraIssueBean implements JiraIssue {

    private final String key;
    private final String name;
    private final String assignee;
    private final double hoursPerUpdate;
    private final String updateRateMetricCategory;
    private final String link;

    @JsonCreator
    public JiraIssueBean(@NotNull @JsonProperty("key") String key,
                         @NotNull @JsonProperty("name") String name,
                         @NotNull @JsonProperty("assignee") String assignee,
                         @JsonProperty("hoursPerUpdate") double hoursPerUpdate,
                         @NotNull @JsonProperty("updateRateMetricCategory") String updateRateMetricCategory,
                         @NotNull @JsonProperty("link") String link) {
        this.key = key;
        this.name = name;
        this.assignee = assignee;
        this.hoursPerUpdate = hoursPerUpdate;
        this.updateRateMetricCategory = updateRateMetricCategory;
        this.link = link;
    }

    @Override
    public @NotNull String getKey() {
        return key;
    }

    @Override
    public @NotNull String getAssignee() {
        return assignee;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public double getHoursPerUpdate() {
        return hoursPerUpdate;
    }

    @Override
    public @NotNull String getUpdateRateMetricCategory() {
        return updateRateMetricCategory;
    }

    @Override
    public @NotNull String getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JiraIssueBean that = (JiraIssueBean) o;

        if (Double.compare(that.hoursPerUpdate, hoursPerUpdate) != 0) return false;
        if (!key.equals(that.key)) return false;
        if (!name.equals(that.name)) return false;
        if (!assignee.equals(that.assignee)) return false;
        if (!updateRateMetricCategory.equals(that.updateRateMetricCategory)) return false;
        return link.equals(that.link);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = key.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + assignee.hashCode();
        temp = Double.doubleToLongBits(hoursPerUpdate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + updateRateMetricCategory.hashCode();
        result = 31 * result + link.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "JiraIssueBean{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", assignee='" + assignee + '\'' +
                ", hoursPerUpdate=" + hoursPerUpdate +
                ", updateRateMetricCategory='" + updateRateMetricCategory + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
