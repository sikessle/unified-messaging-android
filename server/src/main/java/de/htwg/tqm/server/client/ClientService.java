package de.htwg.tqm.server.client;

import de.htwg.tqm.server.beans.Client;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@ThreadSafe
public interface ClientService {

    /**
     * A client can only be registered to the service for a single project. If the client was registered before
     * to another project he will be removed from the previous project.
     */
    void registerClient(@NotNull Client client);

    void unregisterClient(@NotNull String name);

    /**
     * Tries to retrieve the client with the specified name
     */
    @Nullable Client getClient(@NotNull String name);

    /**
     * Returns all clients which have been registered
     */
    @NotNull Collection<Client> getRegisteredClients();

    /**
     * Returns all registered clients for the specified role and project
     */
    @NotNull Collection<Client> getRegisteredClientsFor(@NotNull Client.Role role, @NotNull String project);

}
