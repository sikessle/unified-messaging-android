package de.htwg.tqm.server.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
final class DialogMessageCreateRequestBean {
    private final String user;
    private final String body;

    @JsonCreator
    public DialogMessageCreateRequestBean(@NotNull @JsonProperty("user") String user, @NotNull @JsonProperty("body") String body) {
        this.user = user;
        this.body = body;
    }

    public @NotNull String getUser() {
        return user;
    }


    public @NotNull String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogMessageCreateRequestBean that = (DialogMessageCreateRequestBean) o;

        if (!user.equals(that.user)) return false;
        return body.equals(that.body);

    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DialogMessageCreateRequestBean{" +
                "user='" + user + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
