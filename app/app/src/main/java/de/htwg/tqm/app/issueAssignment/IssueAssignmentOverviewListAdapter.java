package de.htwg.tqm.app.issueAssignment;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.model.JiraUser;
import de.htwg.tqm.app.util.SelfUpdatingAdapter;
import de.htwg.tqm.app.util.DataStorage;

/*
 * Custom adapter for developer list
 * Implements SelfUpdatingAdapter in order to be able to be notified if new data is available
 */
public class IssueAssignmentOverviewListAdapter extends ArrayAdapter<JiraUser> implements
        SharedPreferences.OnSharedPreferenceChangeListener, SelfUpdatingAdapter {

	private final Context context;
    private final int colorGreen;
    private final int colorYellow;
    private final int colorRed;
    private double thresholdGreen;
    private double thresholdYellow;
    private final Comparator<JiraUser> developerComparator;
	
	private static class ViewHolder {
		private TextView itemName;
		private TextView issueCount;
	}

	public IssueAssignmentOverviewListAdapter(Context context, ArrayList<JiraUser> values) {
		super(context, 0, values);
		this.context = context;

        final Resources resources = context.getResources();
        colorGreen = resources.getColor(R.color.issue_green);
        colorYellow = resources.getColor(R.color.issue_yellow);
        colorRed = resources.getColor(R.color.issue_red);
        developerComparator = new issueCountComparator();

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
        setupThresholds();

        this.newDataAvailable();
    }

    private void setupThresholds() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        thresholdGreen = Double.parseDouble(prefs.getString(this.context.getString(
                R.string.key_count_color_threshold_green), "1.0"));
        thresholdYellow = Double.parseDouble(prefs.getString(this.context.getString(
                R.string.key_count_color_threshold_yellow), "2.0"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        final String keyGreen = getContext().getString(
                R.string.key_count_color_threshold_green);
        final String keyYellow = getContext().getString(
                R.string.key_count_color_threshold_green);

        if (keyGreen.equals(key) || keyYellow.equals(key)) {
            setupThresholds();
            notifyDataSetChanged();
        }
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View rowView = convertView;
		
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.issue_assignment_overview_list_item, parent, false);
			
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.itemName = (TextView) rowView.findViewById(R.id.issue_assignment_overview_list_item_name);
			viewHolder.issueCount = (TextView) rowView.findViewById(R.id.issue_assignment_overview_activity_issue_count);
			
			rowView.setTag(viewHolder);
		}
		
		ViewHolder viewHolder = (ViewHolder) rowView.getTag();

		viewHolder.itemName.setText(getItem(position).getName());
		viewHolder.issueCount.setText(Integer.toString(getItem(position).getAssignedIssuesCount()));

        this.setColor(rowView, getItem(position));

		return rowView;
	}

    private void setColor(View rootView, JiraUser jiraUser) {
        final View itemRoot = rootView.findViewById(R.id.issue_assignment_overview_list_item);

        if (jiraUser.getAssignedIssuesCount() <= this.thresholdGreen) {
            itemRoot.setBackgroundColor(this.colorGreen);
        } else if (jiraUser.getAssignedIssuesCount() <= this.thresholdYellow) {
            itemRoot.setBackgroundColor(this.colorYellow);
        } else {
            itemRoot.setBackgroundColor(this.colorRed);
        }
    }

    @Override
	public void newDataAvailable() {
        this.clear();
        this.addAll(DataStorage.getInstance().getUsers());
        notifyDataSetChanged();
	}

    @Override
    public void notifyDataSetChanged() {
        setNotifyOnChange(false);
        sort(developerComparator);
        setNotifyOnChange(true);
        super.notifyDataSetChanged();
    }

    private static class issueCountComparator implements Comparator<JiraUser> {

        @Override
        public int compare(JiraUser lhs, JiraUser rhs) {
            final double lhsRate = lhs.getAssignedIssuesCount();
            final double rhsRate = rhs.getAssignedIssuesCount();

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
