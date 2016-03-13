package de.htwg.tqm.server.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
final class DialogResolveRequestBean {
    private final String user;

    @JsonCreator
    public DialogResolveRequestBean(@NotNull @JsonProperty("user") String user) {
        this.user = user;
    }

    public @NotNull String getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogResolveRequestBean that = (DialogResolveRequestBean) o;

        return user.equals(that.user);

    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }

    @Override
    public String toString() {
        return "DialogResolveRequestBean{" +
                "user='" + user + '\'' +
                '}';
    }
}
