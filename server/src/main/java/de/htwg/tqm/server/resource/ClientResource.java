package de.htwg.tqm.server.resource;

import de.htwg.tqm.server.beans.ClientBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.client.ClientService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("tqm/rest/")
public final class ClientResource {

    @SuppressWarnings("unused")
    @Inject
    private ClientService clientService;

    @POST
    @Path("/clients")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerClient(ClientCreateRequestBean registryMessage) {
        try {
            Client.Role role = Client.Role.valueOf(registryMessage.getRole());
            Client client = new ClientBean(registryMessage.getName(), registryMessage.getProject(), role);
            clientService.registerClient(client);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("/clients/{name}")
    public Response unregisterClient(@PathParam("name") String name) {
        clientService.unregisterClient(name);
        return Response.ok().build();
    }
}
