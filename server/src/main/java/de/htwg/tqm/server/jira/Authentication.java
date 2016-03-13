package de.htwg.tqm.server.jira;

import org.jetbrains.annotations.NotNull;

public interface Authentication {

    /**
     * @return Basic base64encoded(user:pass)
     */
    @NotNull String getBasicAuthValue();

}
