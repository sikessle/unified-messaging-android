package de.htwg.tqm.server.beans;

import org.jetbrains.annotations.NotNull;

public interface DialogMessage extends Comparable<DialogMessage> {

    @NotNull String getAuthor();

    long getTimestamp();

    @NotNull String getBody();
}
