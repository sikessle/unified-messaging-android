package de.htwg.tqm.server.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
final class JiraCloseIssueRequestBean {

    private final GenericFields transition;
    private final GenericFields fields;

    public JiraCloseIssueRequestBean(int transitionID, @NotNull String resolutionName) {
        this.transition = new GenericFields();
        this.transition.id = String.valueOf(transitionID);
        this.fields = new GenericFields();
        this.fields.resolution = new GenericFields();
        this.fields.resolution.name = resolutionName;
    }

    public GenericFields getTransition() {
        return transition;
    }

    public GenericFields getFields() {
        return fields;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GenericFields {
        public String id;
        public GenericFields resolution;
        public String name;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GenericFields that = (GenericFields) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (resolution != null ? !resolution.equals(that.resolution) : that.resolution != null) return false;
            return !(name != null ? !name.equals(that.name) : that.name != null);

        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (resolution != null ? resolution.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JiraCloseIssueRequestBean that = (JiraCloseIssueRequestBean) o;

        if (transition != null ? !transition.equals(that.transition) : that.transition != null) return false;
        return !(fields != null ? !fields.equals(that.fields) : that.fields != null);

    }

    @Override
    public int hashCode() {
        int result = transition != null ? transition.hashCode() : 0;
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }
}
