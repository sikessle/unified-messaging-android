package de.htwg.tqm.app.model;

/**
 * Needed to process all dashboard group objects equally (e.g. inbox group, metric groups)
 */
public interface DashboardGroup {

    // Get type of dashboard group
    GroupType getType();

    // Get number of detail objects of group
    int getSizeOfDetails();

    // Get specific detail object
    DashboardDetail getDetail(final int index);
}
