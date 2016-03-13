package de.htwg.tqm.server.beans;

import org.jetbrains.annotations.NotNull;

public interface JiraComment {

    @NotNull String getAuthor();

    long getTimestamp();

    @NotNull String getContent();

}
