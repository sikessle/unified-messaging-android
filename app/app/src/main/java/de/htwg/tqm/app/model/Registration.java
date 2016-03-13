package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tckeh on 12/9/15.
 */
public class Registration {

    @SerializedName("name")
    private String name;

    @SerializedName("project")
    private String project;

    @SerializedName("role")
    private String role;

    public Registration(final String name, final String project, final String role) {
        this.name = name;
        this.project = project;
        this.role = role;
    }

    public String getName() {
        return this.name;
    }

    public String getProjectKey() {
        return this.project;
    }

    public String getRole() {
        return this.role;
    }
}
