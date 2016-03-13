package de.htwg.tqm.app.issueUpdateQuality;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.model.JiraIssue;
import de.htwg.tqm.app.model.JiraIssueCategory;
import de.htwg.tqm.app.util.DataStorage;
import de.htwg.tqm.app.util.SelfUpdatingAdapter;
import de.htwg.tqm.app.volley.ViewedIssuesHandler;

/**
 * Adapter to display a list of Jira issues.
 */
public final class IssueUpdateQualityListAdapter extends ArrayAdapter<JiraIssue> implements
		OnSharedPreferenceChangeListener, SelfUpdatingAdapter {

	private final int colorGreen;
	private final int colorYellow;
	private final int colorRed;
	private double thresholdGreen;
	private double thresholdYellow;
	private final Comparator<JiraIssue> issueComparator;

	public IssueUpdateQualityListAdapter(Context context, ArrayList<JiraIssue> issues) {
		// Convert the issues array to an array list because ArrayAdapter will
		// convert an array to an AbstractList which cannot be modified later
		// on (this would render the add, addAll etc. methods useless).
		super(context, 0, issues);

		final Resources resources = context.getResources();
		colorGreen = resources.getColor(R.color.issue_green);
		colorYellow = resources.getColor(R.color.issue_yellow);
		colorRed = resources.getColor(R.color.issue_red);
		issueComparator = new LeastHoursPerUpdateComparator();

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		prefs.registerOnSharedPreferenceChangeListener(this);
		setupThresholds();

        this.newDataAvailable();
	}

	private void setupThresholds() {
		thresholdGreen = ViewedIssuesHandler.getThresholdGreen(getContext());
		thresholdYellow = ViewedIssuesHandler.getThresholdYellow(getContext());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		final String keyGreen = getContext().getString(
				R.string.key_quality_color_threshold_green);
		final String keyYellow = getContext().getString(
				R.string.key_quality_color_threshold_green);

		if (keyGreen.equals(key) || keyYellow.equals(key)) {
			setupThresholds();
			notifyDataSetChanged();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		final JiraIssue issue = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.issue_update_quality_overview_list_item, parent, false);
		}

		fillView(convertView, issue);
		setColor(convertView, issue);

		return convertView;
	}

    // View of list elements.
	private void fillView(View rootView, JiraIssue issue) {
		// Lookup view for data population
		final TextView issueItemName = (TextView) rootView
				.findViewById(R.id.issue_update_quality_overview_activity_item_name);
		final TextView issueItemDescription = (TextView) rootView
				.findViewById(R.id.issue_update_quality_overview_activity_item_description);

		// Populate the data into the template view using the data object
		issueItemName.setText(issue.getKey());
		issueItemDescription.setText(Html.fromHtml(buildDescription(issue)));
	}

	private String buildDescription(JiraIssue issue) {
		final Context ctx = getContext();
		final StringBuilder sb = new StringBuilder();

		sb.append(ctx.getString(R.string.hours_per_update));
		sb.append(String.format("<b>%.2f</b>", issue.getHoursPerUpdate()));
		sb.append("\n");
		sb.append("<b>");
		sb.append(ctx.getString(R.string.assignee));
		sb.append("</b>");
		sb.append(issue.getAssignee());

		return sb.toString();
	}

    // Set background color based on the current thresholds.
	private void setColor(View rootView, JiraIssue issue) {
		final View itemRoot = rootView.findViewById(R.id.issueItem);
		final JiraIssueCategory category = JiraIssueCategory.fromIssue(issue,
				thresholdGreen, thresholdYellow);

		switch (category) {
		case GREEN:
			itemRoot.setBackgroundColor(colorGreen);
			break;
		case YELLOW:
			itemRoot.setBackgroundColor(colorYellow);
			break;
		case RED:
			itemRoot.setBackgroundColor(colorRed);
			break;
		}
	}

    // Sort list and notify super class that data has changed.
	@Override
	public void notifyDataSetChanged() {
		setNotifyOnChange(false);
		sort(issueComparator);
		setNotifyOnChange(true);
		super.notifyDataSetChanged();
	}

    // Get new data from data storage and notify the adapter that data has changed.
    @Override
    public void newDataAvailable() {
        this.clear();
        this.addAll(DataStorage.getInstance().getIssues());
        notifyDataSetChanged();
    }

    // Custom comparator for list sorting.
    private static class LeastHoursPerUpdateComparator implements
			Comparator<JiraIssue> {

		@Override
		public int compare(JiraIssue lhs, JiraIssue rhs) {
			final double lhsRate = lhs.getHoursPerUpdate();
			final double rhsRate = rhs.getHoursPerUpdate();

			if (lhsRate > rhsRate) {
				return -1;
			}
			if (lhsRate < rhsRate) {
				return 1;
			}
			return 0;
		}

	}

}
