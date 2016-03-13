package de.htwg.tqm.app.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.util.DataStorage;

/**
 * Dashboard inbox group containing dialog objects
 */
public class DashboardInboxGroup implements DashboardGroup {

    private List<Dialog> dialogs = new ArrayList<>();
    private final String name;
    private final GroupType type;

    public DashboardInboxGroup(final Context context) {
        this.dialogs = DataStorage.getInstance().getDialogs();
        this.name = context.getString(R.string.inbox_activity_title);
        this.type = GroupType.INBOX;
    }

    public String getName() {
        return this.name;
    }

    public int getNumberOfDialogs() {
        return this.dialogs.size();
    }

    public List<Dialog> getDetails() {
        return this.dialogs;
    }

    @Override
    public GroupType getType() {
        return this.type;
    }

    @Override
    public int getSizeOfDetails() {
        return this.dialogs.size();
    }

    @Override
    public DashboardDetail getDetail(int index) {
        return this.dialogs.get(index);
    }
}
