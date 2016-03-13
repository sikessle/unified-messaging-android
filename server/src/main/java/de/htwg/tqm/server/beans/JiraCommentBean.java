package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public final class JiraCommentBean implements JiraComment {
    private final String author;
    private final long timestamp;
    private final String content;

    @JsonCreator
    public JiraCommentBean(@NotNull @JsonProperty("author") String author,
                           @JsonProperty("timestamp") long timestamp,
                           @JsonProperty("content") @NotNull String content) {
        this.author = author;
        this.timestamp = timestamp;
        this.content = content;
    }

    @Override
    public @NotNull String getAuthor() {
        return author;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public @NotNull String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JiraCommentBean that = (JiraCommentBean) o;

        if (timestamp != that.timestamp) return false;
        if (!author.equals(that.author)) return false;
        return content.equals(that.content);

    }

    @Override
    public int hashCode() {
        int result = author.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + content.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "JiraCommentBean{" +
                "author='" + author + '\'' +
                ", timestamp=" + timestamp +
                ", content='" + content + '\'' +
                '}';
    }
}
