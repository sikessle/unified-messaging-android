package de.htwg.tqm.server.beans;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface JiraProject {

    @NotNull String getKey();

    @NotNull String getName();

}
