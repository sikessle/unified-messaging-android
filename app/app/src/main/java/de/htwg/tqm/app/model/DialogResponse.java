package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tckeh on 12/11/15.
 */
public class DialogResponse {

    @SerializedName("dialogID")
    private long dialogID;

    public long getDialogID() {
        return this.dialogID;
    }
}
