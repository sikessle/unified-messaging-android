package de.htwg.tqm.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import de.htwg.tqm.app.model.DashboardGroup;
import de.htwg.tqm.app.model.DashboardMetricDetail;
import de.htwg.tqm.app.model.DashboardInboxGroup;
import de.htwg.tqm.app.model.DashboardMetricGroup;
import de.htwg.tqm.app.model.JiraUser;
import de.htwg.tqm.app.model.Dialog;
import de.htwg.tqm.app.model.JiraIssue;
import de.htwg.tqm.app.model.GroupType;
import de.htwg.tqm.app.settings.MainPreferenceActivity;
import de.htwg.tqm.app.util.DataStorage;
import de.htwg.tqm.app.util.RequestListener;
import de.htwg.tqm.app.util.SelfUpdatingAdapter;
import de.htwg.tqm.app.util.ServerResource;

/**
 * Dashboard giving an overview over the inbox and different metrics.
 * Consists of a expandable list with groups (inbox group and metric groups).
 */
public class DashboardActivity extends Activity {

    private String keyGreenQualityMax;
    private String keyYellowQualityMax;

    String projectName;
    SharedPreferences preferences;

    DashboardMetricGroup qualityGroup;
    DashboardMetricGroup countGroup;
    DashboardInboxGroup inboxGroup;

    DashboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        this.keyGreenQualityMax = this.getString(R.string.key_quality_color_threshold_green);
        this.keyYellowQualityMax = this.getString(R.string.key_quality_color_threshold_yellow);

        final String keyProject = this.getString(R.string.key_project);
        this.projectName = preferences.getString(keyProject, "");

        this.swipeRefreshSetup();

        SparseArray<DashboardGroup> dashboardGroups = this.getNewData();

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.dashboard_activity_list_view);
        this.adapter = new DashboardAdapter(this, dashboardGroups);

        listView.setAdapter(adapter);
    }

    public SparseArray<DashboardGroup> getNewData() {
        // Group setups
        this.inboxSetup();
        this.issueQualityGroupSetup();
        this.issueCountGroupSetup();

        SparseArray<DashboardGroup> dashboardGroups = new SparseArray<>();
        dashboardGroups.put(0, this.inboxGroup);
        dashboardGroups.put(1, this.qualityGroup);
        dashboardGroups.put(2, this.countGroup);

        return dashboardGroups;
    }

    private void swipeRefreshSetup() {
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(
                R.id.dashboard_activity_swipe_refresh_layout);

        // Listener for pull-down refresh; listens to pull-down-refresh layout
        SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                RequestListener listener = new RequestListener(DashboardActivity.this,
                        (SelfUpdatingAdapter) DashboardActivity.this.adapter,
                        swipeRefreshLayout);

                ServerResource.getInstance(DashboardActivity.this).getDialogs(listener);
                ServerResource.getInstance(DashboardActivity.this).getIssues(listener);
                ServerResource.getInstance(DashboardActivity.this).getUsers(listener);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    // Get dialogs from data storage and add to inbox group.
    private void inboxSetup() {
        this.inboxGroup = new DashboardInboxGroup(this);

        ArrayList<Dialog> dialogs = DataStorage.getInstance().getDialogs();
        this.inboxGroup.getDetails().clear();

        for (Dialog dialog : dialogs) {
            this.inboxGroup.getDetails().add(dialog);
        }
    }

    // Count issues with state OK, WARN and CRITICAL. (Shown as doughnut chart in detail object).
    private void issueQualityGroupSetup() {

        // Get thresholds for colors.
        Double greenMax = new Double(preferences.getString(keyGreenQualityMax, "1.0"));
        Double yellowMax = new Double(preferences.getString(keyYellowQualityMax, "2.0"));

        int ok = 0;
        int warn = 0;
        int critical = 0;
        double average = 0.0;

        // Get issues.
        ArrayList<JiraIssue> issues = DataStorage.getInstance().getIssues();

        // Count issues based on their state.
        for (JiraIssue issue : issues) {
            if (issue.getHoursPerUpdate() <= greenMax) {
                ok++;
            } else if (issue.getHoursPerUpdate() <= yellowMax) {
                warn++;
            } else {
                critical++;
            }

            average += issue.getHoursPerUpdate();
        }

        average = average / (double) issues.size();

        // Create and set metric group object (Issue update quality).
        this.qualityGroup = new DashboardMetricGroup(this, projectName, average, GroupType.ISSUE_QUALITY);

        // Add metric detail object (doughnut chart).
        qualityGroup.getDetails().add(new DashboardMetricDetail(ok, warn, critical));
    }

    // Count developers with state OK, WARN and CRITICAL. (Shown as doughnut chart in detail object).
    private void issueCountGroupSetup() {

        // Get thresholds for colors.
        final String keyGreenCountMax = this.getString(R.string.key_count_color_threshold_green);
        final String keyYellowCountMax = this.getString(R.string.key_count_color_threshold_yellow);

        Double greenMax = new Double(preferences.getString(keyGreenCountMax, "1.0"));
        Double yellowMax = new Double(preferences.getString(keyYellowCountMax, "2.0"));

        int ok = 0;
        int warn = 0;
        int critical = 0;
        double average = 0.0;

        // Get jiraUsers.
        ArrayList<JiraUser> jiraUsers = DataStorage.getInstance().getUsers();

        // Count jiraUsers based on their state.
        for (JiraUser jiraUser : jiraUsers) {
            if (jiraUser.getAssignedIssuesCount() <= greenMax) {
                ok++;
            } else if (jiraUser.getAssignedIssuesCount() <= yellowMax) {
                warn++;
            } else {
                critical++;
            }

            average += jiraUser.getAssignedIssuesCount();
        }

        average = average / jiraUsers.size();

        // Create and set metric group object (Issue assignment).
        this.countGroup = new DashboardMetricGroup(this , projectName,
                average, GroupType.ISSUE_COUNT);

        // Add metric detail object (doughnut chart).
        this.countGroup.getDetails().add(new DashboardMetricDetail(ok, warn, critical));
    }

    // Settings menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        final int id = item.getItemId();
        if (id == R.id.settingsAction) {
            final Intent settingsIntent = new Intent(this,
                    MainPreferenceActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
