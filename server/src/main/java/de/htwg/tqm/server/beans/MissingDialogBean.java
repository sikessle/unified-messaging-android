package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.NotNull;


@JsonIgnoreProperties(ignoreUnknown = true)
public final class MissingDialogBean implements MissingDialog {


    private final Notification notification;

    @JsonCreator
    public MissingDialogBean(@NotNull @JsonProperty("notification") @JsonDeserialize(as=NotificationBean.class) Notification notification) {
        this.notification = notification;
    }

    @Override
    public @NotNull Notification getNotification() {
        return notification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MissingDialogBean that = (MissingDialogBean) o;

        return notification.equals(that.notification);

    }

    @Override
    public int hashCode() {
        return notification.hashCode();
    }

    @Override
    public String toString() {
        return "MissingDialogBean{" +
                "notification=" + notification +
                '}';
    }
}
