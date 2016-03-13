package de.htwg.tqm.server.beans;

import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;

@SuppressWarnings("unused")
public interface Dialog {

    long getDialogID();

    @NotNull String getInitiator();

    @NotNull String getAffected();

    @NotNull String getSubject();

    // For jackson!
    long getViolationID();

    // Sorted by timestamp
    @NotNull SortedSet<DialogMessage> getMessages();

    boolean getResolvedInitiator();

    boolean getResolvedAffected();

    long getTimestamp();
}
