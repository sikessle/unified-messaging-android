package de.htwg.tqm.server.beans;

import org.jetbrains.annotations.NotNull;

public interface MissingResponse {

    long getDialogID();

    @NotNull String getUserWhoDidNotRespond();

}
