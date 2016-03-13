package de.htwg.tqm.server.push;

import de.htwg.tqm.server.beans.Notification;

class PendingMessage {
    private final Notification message;

    public PendingMessage(Notification message) {
        this.message = message;
    }

    public Notification getMessage() {
        return message;
    }
}
