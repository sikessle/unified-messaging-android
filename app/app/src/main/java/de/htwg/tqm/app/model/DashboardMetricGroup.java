package de.htwg.tqm.app.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.htwg.tqm.app.R;

/**
 * Metric group containing details objects of metrics (issue update quality, issue assignment)
 */
public class DashboardMetricGroup implements DashboardGroup {

    private final GroupType type;
    private final String name;
    private final double average;
    private final String projectName;
    private final List<DashboardMetricDetail> details = new ArrayList<>();

    public DashboardMetricGroup(final Context context, final String projectName,
                                final double average, final GroupType type) {
        this.projectName = projectName;
        this.average = average;
        this.type = type;

        // Set group name depending on group type
        // Is done here and not in the enum to be language independent (different string.xml)
        switch(this.type) {
            case ISSUE_QUALITY:
                this.name = context.getString(R.string.dashboard_activity_issue_quality_name);
                break;
            case ISSUE_COUNT:
                this.name = context.getString(R.string.dashboard_activity_issue_count_name);
                break;
            default:
                this.name = "Error";
        }
    }

    public String getName() {
        return this.name;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public double getAverage() {
        return this.average;
    }

    public List<DashboardMetricDetail> getDetails() {
        return this.details;
    }

    @Override
    public GroupType getType() {
        return this.type;
    }

    @Override
    public int getSizeOfDetails() {
        return this.details.size();
    }

    @Override
    public DashboardDetail getDetail(int index) {
        return this.details.get(index);
    }
}
