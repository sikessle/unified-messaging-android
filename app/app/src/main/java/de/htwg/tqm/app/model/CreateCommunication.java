package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tckeh on 12/10/15.
 */
public class CreateCommunication {

    @SerializedName("initiator")
    private String initiator;

    @SerializedName("affected")
    private String affected;

    @SerializedName("subject")
    private String subject;

    @SerializedName("violationID")
    private long violationID;

    public CreateCommunication(final String initiator,
                               final String affected,
                               final String subject,
                               final long violationID) {

        this.initiator = initiator;
        this.affected = affected;
        this.subject = subject;
        this.violationID = violationID;
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
}
