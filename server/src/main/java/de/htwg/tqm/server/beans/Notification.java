package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.tqm.server.beans.Client;
import org.jetbrains.annotations.NotNull;

public interface Notification {

    @NotNull Type getType();

    @NotNull Client getReceiver();

    @NotNull ObjectNode getContent();

    long getTimestamp();

    enum Type {
        metricViolation, dialogCreated, missingDialogResponse, dialogMessageCreated
    }
}
