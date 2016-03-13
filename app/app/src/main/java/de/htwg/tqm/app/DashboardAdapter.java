package de.htwg.tqm.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

import de.htwg.tqm.app.communication.InboxDialogActivity;
import de.htwg.tqm.app.model.DashboardGroup;
import de.htwg.tqm.app.model.DashboardInboxGroup;
import de.htwg.tqm.app.model.DashboardMetricDetail;
import de.htwg.tqm.app.model.DashboardMetricGroup;
import de.htwg.tqm.app.model.Dialog;
import de.htwg.tqm.app.model.GroupType;
import de.htwg.tqm.app.issueAssignment.IssueAssignmentOverviewActivity;
import de.htwg.tqm.app.issueUpdateQuality.IssueUpdateQualityOverviewActivity;
import de.htwg.tqm.app.util.SelfUpdatingAdapter;

public class DashboardAdapter extends BaseExpandableListAdapter implements SelfUpdatingAdapter {

    private SparseArray<DashboardGroup> groups;
    public LayoutInflater inflater;
    public DashboardActivity activity;

    public DashboardAdapter(final DashboardActivity activity, SparseArray<DashboardGroup> groups) {
        this.activity = activity;
        this.groups = groups;
        this.inflater = activity.getLayoutInflater();
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded,
                             View view, final ViewGroup viewGroup) {

        // Return expandable group view depending on group type (inbox or metric)
        switch(this.getGroup(groupPosition).getType()) {
            case INBOX:
                return this.getInboxGroupView(groupPosition, isExpanded);
            case ISSUE_QUALITY:
                return this.getMetricGroupView(groupPosition, isExpanded);
            case ISSUE_COUNT:
                return this.getMetricGroupView(groupPosition, isExpanded);
            default:
                return null;
        }
    }

    private View getInboxGroupView(final int groupPosition, final boolean isExpanded) {
        View view = this.inflater.inflate(R.layout.dashboard_listrow_inbox_group, null);

        // Inbox group object containing information about the group itself as well as
        // the dialog objects that contain the messages
        DashboardInboxGroup group = (DashboardInboxGroup) this.getGroup(groupPosition);

        // Set expandable group name to "Inbox" and (don't) expand it
        CheckedTextView groupNameTextView = (CheckedTextView) view.findViewById(
                R.id.dashboard_activity_inbox_name);
        groupNameTextView.setText(group.getName());
        groupNameTextView.setChecked(isExpanded);

        // Set number of messages in inbox
        TextView newMessages = (TextView) view.findViewById(
                R.id.dashboard_activity_inbox_number_of_messages);

        newMessages.setText("Dialogs: " + Integer.toString(group.getNumberOfDialogs()));

        return view;
    }


    private View getMetricGroupView(final int groupPosition, final boolean isExpanded) {

        View view = this.inflater.inflate(R.layout.dashboard_listrow_metric_group, null);

        // Metric group object containing information about the group itself as well as
        // a detail object that contains the numbers of issues / developers with
        // states OK, WARN and CRITICAL
        DashboardMetricGroup group = (DashboardMetricGroup) this.getGroup(groupPosition);

        // Set expandable group name to "Quality" or "Count" and (don't) expand it
        CheckedTextView groupNameTextView = (CheckedTextView) view.findViewById(
                R.id.dashboard_activity_metric_group_name);
        groupNameTextView.setText(group.getName());
        groupNameTextView.setChecked(isExpanded);

        TextView projectNameTextView = (TextView) view.findViewById(
                R.id.dashboard_activity_metric_group_project_name);

        Resources resources = this.activity.getResources();

        // Set project name
        projectNameTextView.setText(String.format("%s: %s", resources.getString(
                R.string.dashboard_activity_project_name), group.getProjectName()));

        TextView averageValueTextView = (TextView) view.findViewById(
                R.id.dashboard_activity_metric_average_value);

        // Set average value of metric
        averageValueTextView.setText(String.format("%.1f", group.getAverage()));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.activity);
        String keyGreenMax = "";
        String keyYellowMax = "";

        // Get keys for thresholds of colors green and yellow depending on the type of the metric
        if (group.getType() == GroupType.ISSUE_QUALITY) {
            keyGreenMax = resources.getString(R.string.key_quality_color_threshold_green);
            keyYellowMax = resources.getString(R.string.key_quality_color_threshold_yellow);
        } else if (group.getType() == GroupType.ISSUE_COUNT) {
            keyGreenMax = resources.getString(R.string.key_count_color_threshold_green);
            keyYellowMax = resources.getString(R.string.key_count_color_threshold_yellow);
        }

        // Get the actual threshold values
        Double greenMax = new Double(preferences.getString(keyGreenMax, "1.0"));
        Double yellowMax = new Double(preferences.getString(keyYellowMax, "2.0"));
        int color = 0;

        // Set background color of group depending on the specific threshold and average value
        if (group.getAverage() <= greenMax) {
            color = resources.getColor(R.color.issue_green);
        } else if (group.getAverage() <= yellowMax) {
            color = resources.getColor(R.color.issue_yellow);
        } else {
            color = resources.getColor(R.color.issue_red);
        }

        if (!isExpanded) {
            view.setBackgroundColor(color);
        }

        return view;

    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             final boolean isLastChild, View view,
                             final ViewGroup viewGroup) {

        // Return expandable group view depending on group type (inbox or metric)
        switch(this.getGroup(groupPosition).getType()) {
            case INBOX:
                return this.getInboxChildView(groupPosition, childPosition);
            case ISSUE_QUALITY:
                return this.getMetricChildView(groupPosition, childPosition);
            case ISSUE_COUNT:
                return this.getMetricChildView(groupPosition, childPosition);
            default:
                return null;
        }
    }

    private View getInboxChildView(final int groupPosition, final int childPosition) {

        View view = this.inflater.inflate(R.layout.dashboard_listrow_inbox_detail, null);

        final DashboardInboxGroup group = (DashboardInboxGroup) this.getGroup(groupPosition);
        final Dialog dialog = group.getDetails().get(childPosition);

        TextView dialogName = (TextView) view.findViewById(R.id.dashboard_activity_inbox_dialog_name);
        dialogName.setText(dialog.getSubject());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardAdapter.this.activity,
                        InboxDialogActivity.class);

                intent.putExtra(DashboardAdapter.this.activity.getString(R.string.key_intent_message),
                        dialog.getDialogID());

                DashboardAdapter.this.activity.startActivity(intent);
            }
        });

        return view;
    }

    private View getMetricChildView(final int groupPosition, final int childPosition) {

        View view = this.inflater.inflate(R.layout.dashboard_listrow_metric_detail, null);

        final DashboardMetricGroup group = (DashboardMetricGroup) this.getGroup(groupPosition);

        this.initializeOverviewButtons(view, group);
        this.initializePieChart(groupPosition, childPosition, view);

        return view;
    }

    private void initializeOverviewButtons(final View view, final DashboardMetricGroup group) {
        Button overviewButton = (Button) view.findViewById(
                R.id.dashboard_activity_metric_detail_overview_button);

        if (group.getType() == GroupType.ISSUE_QUALITY) {
            overviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DashboardAdapter.this.activity,
                            IssueUpdateQualityOverviewActivity.class);
                    DashboardAdapter.this.activity.startActivity(intent);
                }
            });
        } else if (group.getType() == GroupType.ISSUE_COUNT) {
            overviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DashboardAdapter.this.activity,
                            IssueAssignmentOverviewActivity.class);
                    DashboardAdapter.this.activity.startActivity(intent);
                }
            });
        }
    }

    private void initializePieChart(final int groupPosition,
                                    final int childPosition,
                                    final View view) {
        final DashboardMetricDetail detail = (DashboardMetricDetail) this.getChild(
                groupPosition, childPosition);

        // Values to be displayed in pie chart as ArrayList with Entry objects (value, x axis index)
        ArrayList<Entry> pieDataValues = new ArrayList<>();

        ArrayList<Integer> colors = new ArrayList<>();

        // Can be ignored (needed for the PieData constructor)
        ArrayList<String> xAxisLabels = new ArrayList<>();

        int index = 0;

        // Color information for different values
        final Resources resources = this.activity.getResources();

        if (detail.getOk() > 0) {
            pieDataValues.add(new Entry(detail.getOk(), index));
            colors.add(resources.getColor(R.color.issue_green));
            index++;

            // Can be ignored (needed for the PieData constructor)
            xAxisLabels.add("Ok");
        }

        if (detail.getWarn() > 0) {
            pieDataValues.add(new Entry(detail.getWarn(), index));
            colors.add(resources.getColor(R.color.issue_yellow));
            index++;

            // Can be ignored (needed for the PieData constructor)
            xAxisLabels.add("Warn");
        }

        if (detail.getCritical() > 0) {
            pieDataValues.add(new Entry(detail.getCritical(), index));
            colors.add(resources.getColor(R.color.issue_red));

            // Can be ignored (needed for the PieData constructor)
            xAxisLabels.add("Critical");
        }

        // Set of value ArrayList(s)
        PieDataSet pieDataSet = new PieDataSet(pieDataValues, "PieData");
        pieDataSet.setColors(colors);

        // Actual pie chart data (x axis labels (String) and data set (array(s))
        PieData pieData = new PieData(xAxisLabels, pieDataSet);
        pieData.setValueTextSize(15);
        pieData.setValueTextColor(resources.getColor(R.color.white));
        pieData.setValueTypeface(Typeface.DEFAULT_BOLD);

        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                            ViewPortHandler viewPortHandler) {
                return String.format("%.0f", value).toString();
            }
        });

        // Connecting pie chart to layout
        PieChart pieChart = (PieChart) view.findViewById(R.id.dashboard_activity_metric_detail_piechart);

        // Set data and redraw pie chart
        pieChart.setData(pieData);
        pieChart.invalidate();

        // Pie chart specific settings
        pieChart.setDrawSliceText(true);
        pieChart.setHoleColorTransparent(true);
        pieChart.setDragDecelerationEnabled(false);
        pieChart.setDescription("");

        pieChart.getLegend().setEnabled(false);
    }

    @Override
    public int getGroupCount() {
        return this.groups.size();
    }

    @Override
    public int getChildrenCount(final int groupPosition) {
        return this.groups.get(groupPosition).getSizeOfDetails();
    }

    @Override
    public DashboardGroup getGroup(final int groupPosition) {
        return this.groups.get(groupPosition);
    }

    @Override
    public Object getChild(final int groupPosition, final int childPosition) {
        return this.groups.get(groupPosition).getDetail(childPosition);
    }

    @Override
    public long getGroupId(final int position) {
        return 0;
    }

    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }

    @Override
    public void newDataAvailable() {
        this.groups = this.activity.getNewData();
        this.notifyDataSetChanged();
    }
}
