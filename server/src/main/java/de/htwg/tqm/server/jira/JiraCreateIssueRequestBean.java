package de.htwg.tqm.server.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
final class JiraCreateIssueRequestBean {

    private final GenericFields fields;

    public JiraCreateIssueRequestBean(@NotNull String project, @NotNull String assignee, @NotNull String name,
                                      @NotNull String description, @NotNull String issueType) {
        this.fields = new GenericFields();
        this.fields.project = new GenericFields();
        this.fields.project.key = project;
        this.fields.assignee = new GenericFields();
        this.fields.assignee.name = assignee;
        this.fields.summary = name;
        this.fields.description = description;
        this.fields.issuetype = new GenericFields();
        this.fields.issuetype.name = issueType;
    }

    public GenericFields getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JiraCreateIssueRequestBean that = (JiraCreateIssueRequestBean) o;

        return fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GenericFields {
        public GenericFields project;
        public GenericFields assignee;
        public GenericFields issuetype;

        public String key;
        public String name;
        public String summary;
        public String description;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GenericFields that = (GenericFields) o;

            if (project != null ? !project.equals(that.project) : that.project != null) return false;
            if (assignee != null ? !assignee.equals(that.assignee) : that.assignee != null) return false;
            if (issuetype != null ? !issuetype.equals(that.issuetype) : that.issuetype != null) return false;
            if (key != null ? !key.equals(that.key) : that.key != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (summary != null ? !summary.equals(that.summary) : that.summary != null) return false;
            return !(description != null ? !description.equals(that.description) : that.description != null);

        }

        @Override
        public int hashCode() {
            int result = project != null ? project.hashCode() : 0;
            result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
            result = 31 * result + (issuetype != null ? issuetype.hashCode() : 0);
            result = 31 * result + (key != null ? key.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (summary != null ? summary.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            return result;
        }
    }
}
