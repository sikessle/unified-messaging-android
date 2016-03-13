package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tckeh on 12/10/15.
 */
public class DialogResolved {

    @SerializedName("user")
    private String user;

    public DialogResolved(final String user) {
        this.user = user;
    }
}
