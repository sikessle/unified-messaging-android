package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class JiraUserBean implements JiraUser {

    private final String name;
    private final Collection<JiraIssue> assignedIssues;
    private final String assignedIssuesCountMetricCategory;

    public JiraUserBean(@NotNull String name, @NotNull Collection<JiraIssue> assignedIssues,
                        @NotNull String assignedIssuesCountMetricCategory) {
        this.name = name;
        this.assignedIssues = assignedIssues;
        this.assignedIssuesCountMetricCategory = assignedIssuesCountMetricCategory;
    }

    @SuppressWarnings("unused")
    @JsonCreator
    public JiraUserBean(@NotNull @JsonProperty("name") String name,
                        @NotNull @JsonProperty("assignedIssues") @JsonDeserialize(as=JiraIssueBean[].class) JiraIssue[] assignedIssues,
                        @NotNull @JsonProperty("assignedIssuesCountMetricCategory") String assignedIssuesCountMetricCategory) {
        this(name, Arrays.asList(assignedIssues), assignedIssuesCountMetricCategory);
    }


    public @NotNull String getName() {
        return name;
    }

    public @NotNull Collection<JiraIssue> getAssignedIssues() {
        return assignedIssues;
    }

    public int getAssignedIssuesCount() {
        return assignedIssues.size();
    }

    public @NotNull String getAssignedIssuesCountMetricCategory() {
        return assignedIssuesCountMetricCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JiraUserBean that = (JiraUserBean) o;

        if (!name.equals(that.name)) return false;
        if (!assignedIssues.toString().equals(that.assignedIssues.toString())) return false;
        return assignedIssuesCountMetricCategory.equals(that.assignedIssuesCountMetricCategory);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + assignedIssues.hashCode();
        result = 31 * result + assignedIssuesCountMetricCategory.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "JiraUserBean{" +
                "name='" + name + '\'' +
                ", assignedIssues=" + assignedIssues +
                ", assignedIssuesCountMetricCategory='" + assignedIssuesCountMetricCategory + '\'' +
                '}';
    }
}
