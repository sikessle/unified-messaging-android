package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ClientBean implements Client {

    private final String name;
    private final String project;
    private final Role role;

    public ClientBean(@NotNull String name, @NotNull String project, @NotNull Role role) {
        this.name = name;
        this.project = project;
        this.role = role;
    }

    @JsonCreator
    public ClientBean(@NotNull @JsonProperty("name") String name, @NotNull @JsonProperty("project") String project,
                      @NotNull @JsonProperty("role") String role) {
        this(name, project, Role.valueOf(role));
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getProject() {
        return project;
    }

    @Override
    public @NotNull Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientBean client = (ClientBean) o;

        if (!name.equals(client.name)) return false;
        if (!project.equals(client.project)) return false;
        return role == client.role;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + project.hashCode();
        result = 31 * result + role.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ClientBean{" +
                "name='" + name + '\'' +
                ", project='" + project + '\'' +
                ", role=" + role +
                '}';
    }
}
