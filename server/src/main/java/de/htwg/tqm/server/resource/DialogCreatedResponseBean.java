package de.htwg.tqm.server.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
final class DialogCreatedResponseBean {
    private final long dialogID;

    @JsonCreator
    public DialogCreatedResponseBean(@JsonProperty("dialogID") long dialogID) {
        this.dialogID = dialogID;
    }

    public long getDialogID() {
        return dialogID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogCreatedResponseBean that = (DialogCreatedResponseBean) o;

        return dialogID == that.dialogID;

    }

    @Override
    public int hashCode() {
        return (int) (dialogID ^ (dialogID >>> 32));
    }

    @Override
    public String toString() {
        return "DialogCreatedResponseBean{" +
                "dialogID=" + dialogID +
                '}';
    }
}
