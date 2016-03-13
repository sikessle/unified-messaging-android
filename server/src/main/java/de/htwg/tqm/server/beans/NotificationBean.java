package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class NotificationBean implements Notification {

    private final Type type;
    private final long timestamp;
    private final ObjectNode content;
    private final Client receiver;

    public NotificationBean(@NotNull Type type, long timestamp, @NotNull Client receiver, @NotNull ObjectNode content) {
        this.type = type;
        this.timestamp = timestamp;
        this.receiver = receiver;
        this.content = content;
    }

    @JsonCreator
    public NotificationBean(@NotNull @JsonProperty("type") String type,
                            @JsonProperty("timestamp") long timestamp,
                            @NotNull @JsonProperty("receiver") @JsonDeserialize(as = ClientBean.class) Client receiver,
                            @NotNull @JsonProperty("content") ObjectNode content) {
        this(Type.valueOf(type), timestamp, receiver, content);
    }

    @Override
    public @NotNull Type getType() {
        return type;
    }

    @Override
    public @NotNull ObjectNode getContent() {
        return content;
    }

    @Override
    public @NotNull Client getReceiver() {
        return receiver;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationBean that = (NotificationBean) o;

        if (timestamp != that.timestamp) return false;
        if (type != that.type) return false;
        if (!content.equals(that.content)) return false;
        return receiver.equals(that.receiver);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + content.hashCode();
        result = 31 * result + receiver.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NotificationBean{" +
                "type=" + type +
                ", timestamp=" + timestamp +
                ", content=" + content +
                ", receiver=" + receiver +
                '}';
    }
}
