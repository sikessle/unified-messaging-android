package de.htwg.tqm.app.issueUpdateQuality;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.model.JiraIssue;
import de.htwg.tqm.app.settings.MainPreferenceActivity;
import de.htwg.tqm.app.util.DataStorage;

/*
 * Detail view of the update quality of a JIRA issue. Consists of the number of hours between
 * updates and a link to the issue in JIRA.
 */
public class IssueUpdateQualityDetailActivity extends Activity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private JiraIssue issue;
    private int colorGreen;
    private int colorYellow;
    private int colorRed;
    private double thresholdGreen;
    private double thresholdYellow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.issue_update_quality_detail_activity);

        final Resources resources = getResources();
        colorGreen = resources.getColor(R.color.issue_green);
        colorYellow = resources.getColor(R.color.issue_yellow);
        colorRed = resources.getColor(R.color.issue_red);

        // Listen to preference changes to change colors if thresholds are changed.
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        setupThresholds();

        // Get issue key and issue object.
        Intent intent = getIntent();
        String issueKey = intent.getStringExtra(getString(R.string.key_intent_message));
        this.issue = DataStorage.getInstance().getIssue(issueKey);

        // Set hours between updates and appropriate background color.
        TextView issueCount = (TextView) findViewById(R.id.issue_update_quality_detail_activity_quality);
        issueCount.setText(String.format("%.1f", this.issue.getHoursPerUpdate()));
        this.setColor(this.issue);

        // Set issue key.
        TextView issueName = (TextView) findViewById(R.id.issue_update_quality_detail_activity_issue_key);
        issueName.setText(this.issue.getName());

        TextView userKey = (TextView) findViewById(R.id.issue_update_quality_detail_activity_user_key);
        userKey.setText(this.issue.getAssignee());

        // Create link to issue in JIRA.
        TextView issueSelf = (TextView) findViewById(R.id.issue_update_quality_detail_activity_issue_self);
        StringBuilder linkBuilder = new StringBuilder();

        linkBuilder.append("<a href='http://metaproject.in.fhkn.de:8080/browse/")
                .append(issue.getKey())
                .append("'>")
                .append(issue.getKey())
                .append("</a>");

        issueSelf.setText(Html.fromHtml(linkBuilder.toString()));
    }

    // Get thresholds for colors green and yellow from shared preferences.
    private void setupThresholds() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        thresholdGreen = Double.parseDouble(prefs.getString(getString(
                R.string.key_quality_color_threshold_green), "1.0"));
        thresholdYellow = Double.parseDouble(prefs.getString(getString(
                R.string.key_quality_color_threshold_yellow), "2.0"));
    }

    // Set background color based on color thresholds.
    private void setColor(JiraIssue issue) {
        final View view = findViewById(R.id.issue_update_quality_detail_activity_quality_layout);

        if (issue.getHoursPerUpdate() <= this.thresholdGreen) {
            view.setBackgroundColor(this.colorGreen);
        } else if (issue.getHoursPerUpdate() <= this.thresholdYellow) {
            view.setBackgroundColor(this.colorYellow);
        } else {
            view.setBackgroundColor(this.colorRed);
        }
    }

    // Listen to preference changes in order to change color thresholds.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        final String keyGreen = getString(R.string.key_quality_color_threshold_green);
        final String keyYellow = getString(R.string.key_quality_color_threshold_green);

        if (keyGreen.equals(key) || keyYellow.equals(key)) {
            setupThresholds();
        }
    }

    // Open settings menu.
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

    // Starts new issue detail history landscape activity.
    // This is necessary since it is the same context but different data to show.
    // If it were the same data a landscape layout would have been sufficient.
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Intent intent = new Intent(this, IssueUpdateQualityDetailLandscapeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            intent.putExtra(getString(R.string.key_intent_message), this.issue.getKey());

            startActivity(intent);
        }
    }
}
