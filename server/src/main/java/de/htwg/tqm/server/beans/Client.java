package de.htwg.tqm.server.beans;

import org.jetbrains.annotations.NotNull;

public interface Client {

    enum Role {
        SM, DEV
    }


    @NotNull String getName();

    @NotNull String getProject();

    @NotNull Role getRole();


}
