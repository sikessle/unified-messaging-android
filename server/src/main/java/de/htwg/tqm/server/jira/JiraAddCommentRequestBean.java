package de.htwg.tqm.server.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
final class JiraAddCommentRequestBean {

    private final String body;

    public JiraAddCommentRequestBean(@NotNull String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JiraAddCommentRequestBean that = (JiraAddCommentRequestBean) o;

        return body.equals(that.body);

    }

    @Override
    public int hashCode() {
        return body.hashCode();
    }
}
