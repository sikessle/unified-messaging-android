package de.htwg.tqm.server.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
final class ClientCreateRequestBean {
    private final String name;
    private final String project;
    private final String role;

    @JsonCreator
    public ClientCreateRequestBean(@NotNull @JsonProperty("name") String name, @NotNull @JsonProperty("project") String project,
                                   @NotNull @JsonProperty("role") String role) {
        this.name = name;
        this.project = project;
        this.role = role;
    }

    public @NotNull String getName() {
        return name;
    }


    public @NotNull String getProject() {
        return project;
    }


    public @NotNull String getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientCreateRequestBean that = (ClientCreateRequestBean) o;

        if (!name.equals(that.name)) return false;
        if (!project.equals(that.project)) return false;
        return role.equals(that.role);

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
        return "ClientCreateRequestBean{" +
                "name='" + name + '\'' +
                ", project='" + project + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
