package de.htwg.tqm.app.issueAssignment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.model.JiraIssue;
import de.htwg.tqm.app.model.JiraUser;
import de.htwg.tqm.app.settings.MainPreferenceActivity;
import de.htwg.tqm.app.util.DataStorage;

/*
 * Detail view of a jiraUser's open issues. Consists of the number of open issues and
 * links to the issues in JIRA.
 */
public class IssueAssignmentDetailActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

	private JiraUser jiraUser;
    private int colorGreen;
    private int colorYellow;
    private int colorRed;
    private double thresholdGreen;
    private double thresholdYellow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.issue_assignment_detail_activity);

        final Resources resources = getResources();
        colorGreen = resources.getColor(R.color.issue_green);
        colorYellow = resources.getColor(R.color.issue_yellow);
        colorRed = resources.getColor(R.color.issue_red);

        // Listen to preference changes to change colors if thresholds are changed.
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        setupThresholds();

        // Get jiraUser key and jiraUser object.
		Intent intent = getIntent();
		String developerKey = intent.getStringExtra(getString(R.string.key_intent_message));
		this.jiraUser = DataStorage.getInstance().getDeveloper(developerKey);

        // Set number of assigned issues and appropriate background color
		TextView issueCount = (TextView) findViewById(R.id.issue_assignment_detail_activity_issue_count);
		issueCount.setText(Integer.toString(this.jiraUser.getAssignedIssuesCount()));
        this.setColor(this.jiraUser);

        // Set jiraUser's name.
		TextView developerName = (TextView) findViewById(R.id.issue_assignment_detail_activity_developer_name);
		developerName.setText(this.jiraUser.getName());

        // Create list of assigned issues with a hyperlink to the JIRA issue.
		TextView developerIssuesSelfs = (TextView) findViewById(R.id.issue_assignment_detail_activity_issue_selves);
        developerIssuesSelfs.setMovementMethod(new ScrollingMovementMethod());

		for (JiraIssue issue : jiraUser.getAssignedIssues()) {

            StringBuilder linkBuilder = new StringBuilder();

            linkBuilder.append("<a href='")
                        .append(issue.getLink())
                        .append("'>")
                        .append(issue.getKey())
                        .append("</a>");

			developerIssuesSelfs.append(Html.fromHtml(linkBuilder.toString()));
            developerIssuesSelfs.append(String.format("%n%n"));
		}
	}

    // Get thresholds for colors green and yellow from shared preferences.
    private void setupThresholds() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        thresholdGreen = Double.parseDouble(prefs.getString(getString(
                R.string.key_count_color_threshold_green), "1.0"));
        thresholdYellow = Double.parseDouble(prefs.getString(getString(
                R.string.key_count_color_threshold_yellow), "2.0"));
    }

    // Set background color based on color thresholds.
    private void setColor(JiraUser jiraUser) {
        final View view = findViewById(R.id.issue_assignment_detail_activity_issue_count_layout);

        if (jiraUser.getAssignedIssuesCount() <= this.thresholdGreen) {
            view.setBackgroundColor(this.colorGreen);
        } else if (jiraUser.getAssignedIssuesCount() <= this.thresholdYellow) {
            view.setBackgroundColor(this.colorYellow);
        } else {
            view.setBackgroundColor(this.colorRed);
        }
    }

    // Listen to preference changes in order to change color thresholds.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        final String keyGreen = getString(R.string.key_count_color_threshold_green);
        final String keyYellow = getString(R.string.key_count_color_threshold_green);

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

	// Starts new jiraUser detail history landscape activity.
	// This is necessary since it is the same context but different data to show.
    // If it were the same data a landscape layout would have been sufficient.
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Intent intent = new Intent(this, IssueAssignmentDetailLandscapeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

			intent.putExtra(getString(R.string.key_intent_message), this.jiraUser.getName());

			startActivity(intent);
		}
	}
}
