package de.htwg.tqm.server.client;

import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.beans.ClientBean;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class InMemoryClientServiceTest {

    private InMemoryClientService sut;
    private Client client;

    @Before
    public void setUp() throws Exception {
        sut = new InMemoryClientService();
        client = new ClientBean("user1", "project1", Client.Role.DEV);
    }

    @Test
    public void testRegisterClient() throws Exception {
        assertNull(sut.getClient(client.getName()));
        sut.registerClient(client);
        assertThat(sut.getClient(client.getName()), equalTo(client));
    }


    @Test
    public void testUnregisterClient() throws Exception {
        sut.registerClient(client);
        assertNotNull(sut.getClient(client.getName()));
        sut.unregisterClient(client.getName());
        assertNull(sut.getClient(client.getName()));
    }

    @Test
    public void testGetProjectsOfRegisteredClients() throws Exception {
        sut.registerClient(client);
        final Collection<String> projects = sut.getProjectsOfRegisteredClients();
        assertThat(projects.size(), is(1));
        assertThat(projects.iterator().next(), equalTo(client.getProject()));
    }

    @Test
    public void testGetRegisteredClients() throws Exception {
        sut.registerClient(client);
        final Collection<Client> registeredClients = sut.getRegisteredClients();
        assertThat(registeredClients.size(), is(1));
        assertThat(registeredClients.iterator().next(), equalTo(client));
    }

    @Test
    public void testGetRegisteredClientsFor() throws Exception {
        sut.registerClient(client);
        final Collection<Client> clientsForRole = sut.getRegisteredClientsFor(client.getRole(), client.getProject());
        assertThat(clientsForRole.size(), equalTo(1));
        assertTrue(clientsForRole.contains(client));
    }

    @Test
    public void testSwitchProjects() throws Exception {
        Client clientWithOtherProject = new ClientBean(client.getName(), client.getProject() + "2", client.getRole());
        sut.registerClient(client);
        sut.registerClient(clientWithOtherProject);

        // must return clientWithOtherProject
        assertThat(sut.getClient(client.getName()), not(equalTo(client)));
        assertNotNull(sut.getClient(clientWithOtherProject.getName()));
    }
}