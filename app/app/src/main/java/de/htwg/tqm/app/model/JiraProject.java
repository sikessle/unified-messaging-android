package de.htwg.tqm.app.model;

import com.google.gson.annotations.SerializedName;

import net.jcip.annotations.Immutable;

/**
 * Represents a Jira project resource with some, not all, properties. This class
 * will be instantiated by GSON and filled via reflection.
 */
@Immutable
public final class JiraProject {

    @SerializedName("key")
	private String key;

    @SerializedName("name")
	private String name;

	public String getKey() {
		return this.key;
	}

	public String getName() {
		return this.name;
	}
}
