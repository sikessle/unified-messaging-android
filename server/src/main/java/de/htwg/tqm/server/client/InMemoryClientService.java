package de.htwg.tqm.server.client;

import de.htwg.tqm.server.beans.Client;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
@Singleton
public class InMemoryClientService implements ClientService {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(InMemoryClientService.class);

    private final Map<String, Client> clients = new ConcurrentHashMap<>();

    @Override
    public void registerClient(@NotNull Client client) {
        clients.put(client.getName(), client);
        LOG.debug("Client registered: {}", client.toString());
    }

    @Override
    public void unregisterClient(@NotNull String name) {
        clients.remove(name);
        LOG.debug("Client unregistered: {}", name);
    }

    @Override
    public @Nullable Client getClient(@NotNull String name) {
        return clients.get(name);
    }

    public @NotNull Collection<String> getProjectsOfRegisteredClients() {
        Collection<String> projects = new HashSet<>();
        clients.values().forEach(client -> projects.add(client.getProject()));
        return projects;
    }

    @Override
    public @NotNull Collection<Client> getRegisteredClients() {
        return Collections.unmodifiableCollection(clients.values());
    }

    @Override
    public @NotNull Collection<Client> getRegisteredClientsFor(@NotNull Client.Role role, @NotNull String project) {
        Collection<Client> matchingClients = new ArrayList<>();
        clients.values().forEach(client -> {
            if (client.getRole().equals(role) && client.getProject().equals(project)) {
                matchingClients.add(client);
            }
        });
        return matchingClients;
    }
}
