package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class DialogBean implements Dialog {

    private final long dialogID;
    private final long violationID;
    private final String subject;
    private final SortedSet<DialogMessage> messages;
    private final boolean resolvedInitiator;
    private final boolean resolvedAffected;
    private final String initiator;
    private final String affected;
    private final long timestamp;


    public DialogBean(long dialogID, @NotNull String subject, long violationID, @NotNull SortedSet<DialogMessage> messages,
                      @NotNull String initiator, @NotNull String affected, boolean resolvedInitiator, boolean resolvedAffected,
                      long timestamp) {
        this.dialogID = dialogID;
        this.subject = subject;
        this.messages = messages;
        this.violationID = violationID;
        this.resolvedInitiator = resolvedInitiator;
        this.resolvedAffected = resolvedAffected;
        this.initiator = initiator;
        this.affected = affected;
        this.timestamp = timestamp;
    }

    @JsonCreator
    public DialogBean(@JsonProperty("dialogID") long dialogID,
                      @NotNull @JsonProperty("subject") String subject,
                      @JsonProperty("violationID") long violationID,
                      @NotNull @JsonProperty("messages") @JsonDeserialize(as = DialogMessageBean[].class) DialogMessage[] messages,
                      @NotNull @JsonProperty("initiator") String initiator,
                      @NotNull @JsonProperty("affected") String affected,
                      @JsonProperty("resolvedInitiator") boolean resolvedInitiator,
                      @JsonProperty("resolvedAffected") boolean resolvedAffected,
                      @JsonProperty("timestamp") long timestamp) {
        this(dialogID, subject, violationID, new TreeSet<>(Arrays.asList(messages)), initiator, affected, resolvedInitiator, resolvedAffected, timestamp);
    }

    @Override
    public long getDialogID() {
        return dialogID;
    }

    @Override
    public long getViolationID() {
        return violationID;
    }

    @Override
    public @NotNull String getInitiator() {
        return initiator;
    }

    @Override
    public @NotNull String getAffected() {
        return affected;
    }

    @Override
    public @NotNull String getSubject() {
        return subject;
    }

    @Override
    public @NotNull SortedSet<DialogMessage> getMessages() {
        return messages;
    }

    @Override
    public boolean getResolvedInitiator() {
        return resolvedInitiator;
    }

    @Override
    public boolean getResolvedAffected() {
        return resolvedAffected;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogBean that = (DialogBean) o;

        if (dialogID != that.dialogID) return false;
        if (violationID != that.violationID) return false;
        if (resolvedInitiator != that.resolvedInitiator) return false;
        if (resolvedAffected != that.resolvedAffected) return false;
        if (!subject.equals(that.subject)) return false;
        if (timestamp != that.timestamp) return false;
        if (!messages.toString().equals(that.messages.toString())) return false;
        if (!initiator.equals(that.initiator)) return false;
        return affected.equals(that.affected);

    }

    @Override
    public int hashCode() {
        int result = (int) (dialogID ^ (dialogID >>> 32));
        result = 31 * result + (int) (violationID ^ (violationID >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + subject.hashCode();
        result = 31 * result + messages.hashCode();
        result = 31 * result + (resolvedInitiator ? 1 : 0);
        result = 31 * result + (resolvedAffected ? 1 : 0);
        result = 31 * result + initiator.hashCode();
        result = 31 * result + affected.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DialogBean{" +
                "dialogID=" + dialogID +
                ", subject='" + subject + '\'' +
                ", violationID='" + violationID + '\'' +
                ", messages=" + messages +
                ", resolvedInitiator=" + resolvedInitiator +
                ", resolvedAffected=" + resolvedAffected +
                ", initiator='" + initiator + '\'' +
                ", affected='" + affected + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
