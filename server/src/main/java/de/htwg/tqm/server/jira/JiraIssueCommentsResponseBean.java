package de.htwg.tqm.server.jira;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import de.htwg.tqm.server.beans.JiraComment;
import de.htwg.tqm.server.beans.JiraCommentBean;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
final class JiraIssueCommentsResponseBean {

    private final Collection<JiraComment> cleanedComments = new ArrayList<>();
    private final ISO8601DateFormat iso8601DateFormat = new ISO8601DateFormat();

    @JsonCreator
    public JiraIssueCommentsResponseBean(@JsonProperty("fields") @NotNull GenericFields fields) {
        if (fields.comment != null && fields.comment.comments != null) {
            extractComments(fields.comment.comments);
        }
    }

    private void extractComments(GenericFields[] comments) {
        String author;
        long timestamp;
        String content;
        for (GenericFields comment : comments) {
            author = comment.author.name;
            timestamp = getMillisFromDateString(comment.created);
            content = comment.body;

            this.cleanedComments.add(new JiraCommentBean(author, timestamp, content));
        }
    }

    private long getMillisFromDateString(@NotNull String created) {
        try {
            return iso8601DateFormat.parse(created).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Collection<JiraComment> getComments() {
        return cleanedComments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JiraIssueCommentsResponseBean that = (JiraIssueCommentsResponseBean) o;

        return !(cleanedComments != null ? !cleanedComments.equals(that.cleanedComments) : that.cleanedComments != null);

    }

    @Override
    public int hashCode() {
        return cleanedComments != null ? cleanedComments.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "JiraIssueCommentsResponseBean{" +
                "cleanedComments=" + cleanedComments +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GenericFields {
        public GenericFields comment;
        public GenericFields[] comments;
        public GenericFields author;

        public String name;
        public String body;
        public String created;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GenericFields that = (GenericFields) o;

            if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(comments, that.comments)) return false;
            if (author != null ? !author.equals(that.author) : that.author != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (body != null ? !body.equals(that.body) : that.body != null) return false;
            return !(created != null ? !created.equals(that.created) : that.created != null);

        }

        @Override
        public int hashCode() {
            int result = comment != null ? comment.hashCode() : 0;
            result = 31 * result + Arrays.hashCode(comments);
            result = 31 * result + (author != null ? author.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (body != null ? body.hashCode() : 0);
            result = 31 * result + (created != null ? created.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "GenericFields{" +
                    "comment=" + comment +
                    ", comments=" + Arrays.toString(comments) +
                    ", author=" + author +
                    ", name='" + name + '\'' +
                    ", body='" + body + '\'' +
                    ", created='" + created + '\'' +
                    '}';
        }
    }


}