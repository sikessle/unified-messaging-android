package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class MissingResponseBean implements MissingResponse {


    private final long dialogID;
    private final String userWhoDidNotRespond;

    @JsonCreator
    public MissingResponseBean(@JsonProperty("dialogID") long dialogID,
            @NotNull @JsonProperty("userWhoDidNotRespond") String userWhoDidNotRespond) {

        this.dialogID = dialogID;
        this.userWhoDidNotRespond = userWhoDidNotRespond;
    }

    @Override
    public long getDialogID() {
        return dialogID;
    }

    @Override
    public @NotNull String getUserWhoDidNotRespond() {
        return userWhoDidNotRespond;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MissingResponseBean that = (MissingResponseBean) o;

        if (dialogID != that.dialogID) return false;
        return userWhoDidNotRespond.equals(that.userWhoDidNotRespond);

    }

    @Override
    public int hashCode() {
        int result = (int) (dialogID ^ (dialogID >>> 32));
        result = 31 * result + userWhoDidNotRespond.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MissingResponseBean{" +
                "dialogID=" + dialogID +
                ", userWhoDidNotRespond='" + userWhoDidNotRespond + '\'' +
                '}';
    }
}
