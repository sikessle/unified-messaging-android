package de.htwg.tqm.server.communication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.Dialog;
import de.htwg.tqm.server.beans.DialogBean;
import de.htwg.tqm.server.beans.Client;
import de.htwg.tqm.server.beans.DialogMessage;
import de.htwg.tqm.server.persistence.PersistenceService;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;

@ThreadSafe
@Singleton
public final class NonContextAwareDialogService implements DialogService {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(NonContextAwareDialogService.class);

    private static final String COLLECTION = "dialogs";
    static final String KEY_DIALOG_HANDLER_IDENT = "dialogHandlerIdent";
    static final String KEY_SUBJECT = "subject";
    static final String KEY_INITIATOR = "initiator";
    static final String KEY_AFFECTED = "affected";
    static final String KEY_INITIATOR_RESOLVED = "resolvedInitiator";
    static final String KEY_AFFECTED_RESOLVED = "resolvedAffected";
    static final String KEY_VIOLATION_ID = "violationID";
    private final PersistenceService.Collection collection;
    private final ObjectMapper mapper = new ObjectMapper();
    private final DialogHandler dialogHandler;
    private final String handlerIdentifier;

    @Inject
    public NonContextAwareDialogService(@NotNull PersistenceService persistenceService, @NotNull DialogHandler dialogHandler) {
        this.collection = persistenceService.getCollection(COLLECTION);
        this.dialogHandler = dialogHandler;
        handlerIdentifier = dialogHandler.getUniqueHandlerIdentifier();
    }

    @Override
    public long createDialog(@NotNull String subject, long violationID, @NotNull Client initiator, @NotNull Client affected) {
        final long dialogID = getNextFreeKey();
        final String handlerDialogID = dialogHandler.createDialog(subject, initiator, affected);
        final ObjectNode dialogInfo = mapper.createObjectNode();
        dialogInfo.put(KEY_DIALOG_HANDLER_IDENT, handlerIdentifier);
        dialogInfo.put(handlerIdentifier, handlerDialogID);
        dialogInfo.put(KEY_SUBJECT, subject);
        dialogInfo.put(KEY_VIOLATION_ID, violationID);
        dialogInfo.put(KEY_INITIATOR, initiator.getName());
        dialogInfo.put(KEY_AFFECTED, affected.getName());
        dialogInfo.put(KEY_INITIATOR_RESOLVED, false);
        dialogInfo.put(KEY_AFFECTED_RESOLVED, false);
        collection.store(String.valueOf(dialogID), dialogInfo);

        return dialogID;
    }

    private synchronized long getNextFreeKey() {
        synchronized (collection) {
            final SortedSet<String> keys = collection.loadKeys();
            if (keys.isEmpty()) {
                return 0;
            } else {
                return Long.valueOf(keys.last()) + 1;
            }
        }
    }


    @Override
    public @Nullable Dialog getDialog(long dialogID) {
        final JsonNode dialogInfo = getDialogInfo(dialogID);
        if (dialogInfo == null || currentDialogHandlerIsWrong(dialogInfo)) {
            return null;
        }

        String dialogHandlerDialogID = getDialogHandlerDialogID(dialogInfo);
        SortedSet<DialogMessage> messages = dialogHandler.getDialogMessages(dialogHandlerDialogID);
        String subject = getSubject(dialogInfo);
        String initiator = getInitiator(dialogInfo);
        String affected = getAffected(dialogInfo);
        boolean resolvedInitiator = getResolvedInitiator(dialogInfo);
        boolean resolvedAffected = getResolvedAffected(dialogInfo);
        long violationID = getViolationID(dialogInfo);

        return new DialogBean(dialogID, subject, violationID, messages, initiator, affected, resolvedInitiator, resolvedAffected, Instant.now().toEpochMilli());
    }

    private long getViolationID(@NotNull JsonNode dialogInfo) {
        return dialogInfo.get(KEY_VIOLATION_ID).asLong();
    }

    private boolean getResolvedInitiator(@NotNull JsonNode dialogInfo) {
        return dialogInfo.get(KEY_INITIATOR_RESOLVED).asBoolean();
    }

    private boolean getResolvedAffected(@NotNull JsonNode dialogInfo) {
        return dialogInfo.get(KEY_AFFECTED_RESOLVED).asBoolean();
    }

    private @NotNull String getInitiator(@NotNull JsonNode dialogInfo) {
        return dialogInfo.get(KEY_INITIATOR).textValue();
    }

    private @NotNull String getAffected(@NotNull JsonNode dialogInfo) {
        return dialogInfo.get(KEY_AFFECTED).textValue();
    }

    private @NotNull String getSubject(JsonNode dialogInfo) {
        return dialogInfo.get(KEY_SUBJECT).textValue();
    }

    private boolean currentDialogHandlerIsWrong(@NotNull JsonNode dialogInfo) {
        String dialogHandlerIdentifier = dialogInfo.get(KEY_DIALOG_HANDLER_IDENT).textValue();
        return !handlerIdentifier.equals(dialogHandlerIdentifier);
    }

    @Override
    public @NotNull Collection<Dialog> getDialogsForParticipant(@NotNull Client participant) {
        Collection<Dialog> dialogs = new ArrayList<>();
        String participantName = participant.getName();
        collection.loadKeys().forEach(dialogID -> {
            Dialog dialog = getDialog(Long.valueOf(dialogID));
            if (dialog != null
                    && (dialog.getInitiator().equals(participantName) || dialog.getAffected().equals(participantName))) {
                dialogs.add(dialog);
            }
        });
        return dialogs;
    }

    @Override
    public boolean isDialogExistingForViolationID(long violationID) {
        for (JsonNode dialogInfo : collection.loadValues()) {
            if (dialogInfo.get(KEY_VIOLATION_ID).asLong() == violationID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addMessage(long dialogID, @NotNull DialogMessage message) {
        JsonNode dialogInfo = getDialogInfo(dialogID);
        if (dialogInfo == null) {
            return;
        }
        String dialogHandlerDialogID = getDialogHandlerDialogID(dialogInfo);
        dialogHandler.addMessage(dialogHandlerDialogID, message);
    }

    @Override
    public synchronized void markDialogAsResolved(long dialogID, @NotNull Client participant) {
        ObjectNode dialogInfo = (ObjectNode) getDialogInfo(dialogID);
        if (dialogInfo == null) {
            return;
        }

        // Check both condition separately, as in a special case initiator may be the same as the affected

        // Is affected
        if (getAffected(dialogInfo).equals(participant.getName())) {
            dialogInfo.put(KEY_AFFECTED_RESOLVED, true);
        }

        // Is initiator
        if (getInitiator(dialogInfo).equals(participant.getName())) {
            dialogInfo.put(KEY_INITIATOR_RESOLVED, true);
        }

        LOG.debug("Dialog resolved by {}", participant.getName());

        if (getResolvedInitiator(dialogInfo) && getResolvedAffected(dialogInfo)) {
            LOG.debug("Dialog is completely resolved");
            dialogHandler.onMarkDialogAsResolved(getDialogHandlerDialogID(dialogInfo));
        }

        collection.store(String.valueOf(dialogID), dialogInfo);
    }

    private @Nullable JsonNode getDialogInfo(long dialogID) {
        return collection.load(String.valueOf(dialogID));
    }

    private @NotNull String getDialogHandlerDialogID(JsonNode dialogInfo) {
        return dialogInfo.get(getKeyForHandlerDialogID(dialogInfo)).textValue();
    }

    private @NotNull String getKeyForHandlerDialogID(JsonNode dialogInfo) {
        return dialogInfo.get(KEY_DIALOG_HANDLER_IDENT).textValue();
    }

}
