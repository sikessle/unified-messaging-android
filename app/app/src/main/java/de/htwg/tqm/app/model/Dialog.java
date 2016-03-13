package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Inbox Dialog model class. Represents an inbox dialog consisting of its key and messages,
 * sent from different developers.
 */
public class Dialog implements DashboardDetail {

    @SerializedName("dialogID")
    private long dialogID;

    @SerializedName("resolvedInitiator")
    private boolean resolvedInitiator;

    @SerializedName("resolvedAffected")
    private boolean resolvedAffected;

    @SerializedName("initiator")
    private String initiator;

    @SerializedName("affected")
    private String affected;

    @SerializedName("subject")
    private String subject;

    @SerializedName("violationID")
    private long violationID;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("messages")
    private List<Message> messages;

    public Dialog(final long dialogID,
                  final String initiator,
                  final String affected,
                  final String subject,
                  final long violationID) {

        this.dialogID = dialogID;
        this.resolvedInitiator = false;
        this.resolvedAffected = false;
        this.initiator = initiator;
        this.affected = affected;
        this.subject = subject;
        this.violationID = violationID;
        this.timestamp = 0;
        this.messages = new ArrayList<>();
    }

    public long getDialogID() {
        return this.dialogID;
    }

    public boolean isResolvedInitiator() {
        return this.resolvedInitiator;
    }

    public boolean isResolvedAffected() {
        return this.resolvedAffected;
    }

    public String getInitiator() {
        return this.initiator;
    }

    public String getAffected() {
        return this.affected;
    }

    public String getSubject() {
        return this.subject;
    }

    public long getViolationID() {
        return this.violationID;
    }

    public List<Message> getMessages() {
        return this.messages;
    }
}
