package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tckeh on 12/10/15.
 */
public class NewMessage {

    @SerializedName("user")
    private String user;

    @SerializedName("body")
    private String body;

    public NewMessage(final String user, final String body) {
        this.user = user;
        this.body = body;
    }
}
