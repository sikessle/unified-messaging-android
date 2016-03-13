package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

/**
 * Note: compareTo is not implemented in the same ways as equals and hashcode.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DialogMessageBean implements DialogMessage {

    private final String author;
    private long timestamp;
    private final String body;

    @JsonCreator
    public DialogMessageBean(@NotNull @JsonProperty("author") String author,
                             @JsonProperty("timestamp") long timestamp,
                             @JsonProperty("body") @NotNull String body) {
        this.author = author;
        this.timestamp = timestamp;
        this.body = body;
    }


    @Override
    public @NotNull String getAuthor() {
        return author;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public @NotNull String getBody() {
        return body;
    }

    @Override
    public int compareTo(DialogMessage o) {
        if (timestamp < o.getTimestamp()) {
            return -1;
        }
        if (timestamp > o.getTimestamp()) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogMessageBean that = (DialogMessageBean) o;

        if (timestamp != that.timestamp) return false;
        if (!author.equals(that.author)) return false;
        return body.equals(that.body);

    }

    @Override
    public int hashCode() {
        int result = author.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + body.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DialogMessageBean{" +
                "author='" + author + '\'' +
                ", timestamp=" + timestamp +
                ", body='" + body + '\'' +
                '}';
    }
}
