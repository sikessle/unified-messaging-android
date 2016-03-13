package de.htwg.tqm.app.util;

/**
 * Interface for list adapters to be able to get notified when new data is available.
 */
public interface SelfUpdatingAdapter {
    public void newDataAvailable();
}
