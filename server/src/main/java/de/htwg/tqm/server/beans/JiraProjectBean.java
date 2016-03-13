package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class JiraProjectBean implements JiraProject {

    private final String key;
    private final String name;

    @JsonCreator
    public JiraProjectBean(@NotNull @JsonProperty("key") String key,
                           @NotNull @JsonProperty("name") String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public @NotNull String getKey() {
        return key;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JiraProjectBean that = (JiraProjectBean) o;

        if (!key.equals(that.key)) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "JiraProjectBean{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
