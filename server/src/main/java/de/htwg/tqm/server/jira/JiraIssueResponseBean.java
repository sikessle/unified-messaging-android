package de.htwg.tqm.server.jira;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

@JsonIgnoreProperties(ignoreUnknown = true)
final class JiraIssueResponseBean {

    private final String key;
    private final String name;
    private final String assignee;
    private final double hoursPerUpdate;

    @JsonCreator
    public JiraIssueResponseBean(@NotNull @JsonProperty("key") String key,
                                 @JsonProperty("fields") @Nullable GenericFields fields) {
        this.key = key;
        if (fields != null) {
            this.name = fields.summary;
            this.assignee = fields.assignee != null ? fields.assignee.name : "";
            this.hoursPerUpdate = getHoursPerUpdate(fields);
        } else {
            this.name = "";
            this.assignee = "";
            this.hoursPerUpdate = 0.0;
        }
    }

    private double getHoursPerUpdate(@NotNull GenericFields rootFields) {
        if (rootFields.worklog == null
                || rootFields.timetracking == null
                || rootFields.worklog.total == 0) {
            return 0;
        } else {
            return TimeUnit.SECONDS.toMinutes(rootFields.timetracking.timeSpentSeconds)
                    / 60. / rootFields.worklog.total;
        }
    }

    public @NotNull String getKey() {
        return key;
    }

    public @NotNull String getAssignee() {
        return assignee;
    }

    public @NotNull String getName() {
        return name;
    }


    public double getHoursPerUpdate() {
        return hoursPerUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JiraIssueResponseBean that = (JiraIssueResponseBean) o;

        if (Double.compare(that.hoursPerUpdate, hoursPerUpdate) != 0) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(assignee != null ? !assignee.equals(that.assignee) : that.assignee != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = key != null ? key.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
        temp = Double.doubleToLongBits(hoursPerUpdate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "JiraIssueResponseBean{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", assignee='" + assignee + '\'' +
                ", hoursPerUpdate=" + hoursPerUpdate +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GenericFields {
        public String summary;
        public String name;
        public int total;
        public long timeSpentSeconds;

        public GenericFields assignee;
        public GenericFields worklog;
        public GenericFields timetracking;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GenericFields that = (GenericFields) o;

            if (total != that.total) return false;
            if (timeSpentSeconds != that.timeSpentSeconds) return false;
            if (summary != null ? !summary.equals(that.summary) : that.summary != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (assignee != null ? !assignee.equals(that.assignee) : that.assignee != null) return false;
            if (worklog != null ? !worklog.equals(that.worklog) : that.worklog != null) return false;
            return !(timetracking != null ? !timetracking.equals(that.timetracking) : that.timetracking != null);

        }

        @Override
        public int hashCode() {
            int result = summary != null ? summary.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + total;
            result = 31 * result + (int) (timeSpentSeconds ^ (timeSpentSeconds >>> 32));
            result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
            result = 31 * result + (worklog != null ? worklog.hashCode() : 0);
            result = 31 * result + (timetracking != null ? timetracking.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "GenericFields{" +
                    "summary='" + summary + '\'' +
                    ", name='" + name + '\'' +
                    ", total=" + total +
                    ", timeSpentSeconds=" + timeSpentSeconds +
                    ", assignee=" + assignee +
                    ", worklog=" + worklog +
                    ", timetracking=" + timetracking +
                    '}';
        }
    }


}