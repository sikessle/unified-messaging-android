package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Message model class. Represents an inbox dialog message that contains its key, sender and
 * content.
 */
public class Message {

    @SerializedName("author")
    private String user;

    @SerializedName("body")
    private String body;

    public String getUser() {
        return this.user;
    }

    public String getBody() {
        return this.body;
    }
}
