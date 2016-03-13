package de.htwg.tqm.app.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by tckeh on 12/11/15.
 */
public class WebSocketNotification {

    @SerializedName("type")
    private String type;

    @SerializedName("content")
    private JsonObject content;

    public String getType() {
        return this.type;
    }

    public JsonObject getContent() {
        return this.content;
    }

    @Override
    public String toString() {
        return this.content.toString();
    }
}
