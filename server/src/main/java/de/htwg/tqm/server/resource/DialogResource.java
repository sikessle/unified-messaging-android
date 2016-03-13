package de.htwg.tqm.server.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.*;
import de.htwg.tqm.server.client.ClientService;
import de.htwg.tqm.server.communication.DialogResponseWatcherService;
import de.htwg.tqm.server.communication.DialogService;
import de.htwg.tqm.server.push.PushService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collection;

@Path("tqm/rest/")
public final class DialogResource {

    @SuppressWarnings("unused")
    @Inject
    private ClientService clientService;
    @SuppressWarnings("unused")
    @Inject
    private DialogService dialogService;
    @SuppressWarnings("unused")
    @Inject
    private PushService pushService;
    @SuppressWarnings("unused")
    @Inject
    private DialogResponseWatcherService responseWatcher;

    private final ObjectMapper mapper = new ObjectMapper();


    @POST
    @Path("/dialogs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDialog(DialogCreateRequestBean createBean) {
        Client initiator = clientService.getClient(createBean.getInitiator());
        Client affected = clientService.getClient(createBean.getAffected());

        if (initiator == null || affected == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long dialogID = dialogService.createDialog(createBean.getSubject(), createBean.getViolationID(), initiator, affected);
        DialogCreatedResponseBean dialogCreated = new DialogCreatedResponseBean(dialogID);

        // Notify the affected person about the dialog.
        final ObjectNode notificationContent = mapper.createObjectNode();
        notificationContent.put("dialogID", dialogID);
        Notification notification = new NotificationBean(Notification.Type.dialogCreated, Instant.now().toEpochMilli(), affected, notificationContent);
        pushService.send(notification);

        // Watch the dialog for responses
        responseWatcher.watchDialog(dialogID);

        return Response.ok(dialogCreated).build();
    }

    @GET
    @Path("/dialogs/{dialogID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDialog(@PathParam("dialogID") int dialogID) {
        final Dialog dialog = dialogService.getDialog(dialogID);

        if (dialog == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(dialog).build();
    }

    @GET
    @Path("/dialogs/users/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDialogsForUsername(@PathParam("username") String username) {
        final Client client = clientService.getClient(username);

        if (client == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Collection<Dialog> dialogs = dialogService.getDialogsForParticipant(client);

        return Response.ok(dialogs.toArray(new Dialog[dialogs.size()])).build();
    }

    @POST
    @Path("/dialogs/{dialogID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createMessageToDialog(@PathParam("dialogID") int dialogID, DialogMessageCreateRequestBean messageRequestBean) {
        final Dialog dialog = dialogService.getDialog(dialogID);

        if (dialog == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final DialogMessage message = new DialogMessageBean(messageRequestBean.getUser(), Instant.now().toEpochMilli(), messageRequestBean.getBody());

        dialogService.addMessage(dialogID, message);

        // Notify the other person of the dialog about the new message
        final Client other = getDialogPartner(message.getAuthor(), dialog);
        if (other != null) {
            final ObjectNode notificationContent = mapper.createObjectNode();
            notificationContent.put("dialogID", dialogID);
            Notification notification = new NotificationBean(Notification.Type.dialogMessageCreated, Instant.now().toEpochMilli(), other, notificationContent);
            pushService.send(notification);
        }

        return Response.ok().build();
    }

    private @Nullable Client getDialogPartner(@NotNull String person, @NotNull Dialog dialog) {
        if (person.equals(dialog.getInitiator())) {
            return clientService.getClient(dialog.getAffected());
        }
        return clientService.getClient(dialog.getInitiator());
    }


    @POST
    @Path("/dialogs/{dialogID}/resolve")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resolveDialog(@PathParam("dialogID") int dialogID, DialogResolveRequestBean resolveBean) {
        final Client participant = clientService.getClient(resolveBean.getUser());
        final Dialog dialog = dialogService.getDialog(dialogID);

        if (participant == null || dialog == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            dialogService.markDialogAsResolved(dialogID, participant);
        }

        return Response.ok().build();
    }


}
