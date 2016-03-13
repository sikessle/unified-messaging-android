package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tckeh on 12/14/15.
 */
public class Resolve {

    @SerializedName("user")
    private String user;

    public Resolve(final String user) {
        this.user = user;
    }

    public String getUser() {
        return this.user;
    }
}
