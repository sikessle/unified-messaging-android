package de.htwg.tqm.app.issueUpdateQuality;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.util.ArrayList;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.settings.MainPreferenceActivity;
import de.htwg.tqm.app.util.DataStorage;

public class IssueUpdateQualityDetailLandscapeActivity extends Activity {

    private String issueKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.issue_update_quality_detail_landscape_activity);

        // Get issue key from intent extra.
        Intent intent = getIntent();
        this.issueKey = intent.getStringExtra(getString(R.string.key_intent_message));

        this.initializeBarChart();
    }

    // Initialize bar chart with historic data from the last five days.
    private void initializeBarChart() {
        ArrayList<Float> rawValues = DataStorage.getInstance().getIssueHistory(this.issueKey);
        ArrayList<BarEntry> chartValues = new ArrayList<>();

        // Convert raw values (floats) into bar chart entries.
        for (int i = 0; i < rawValues.size(); i++) {
            chartValues.add(new BarEntry(rawValues.get(i), i));
        }

        // X axis labels for every x axis value.
        ArrayList<String> xAxisLabels = new ArrayList<>();
        xAxisLabels.add("Mon");
        xAxisLabels.add("Tue");
        xAxisLabels.add("Wed");
        xAxisLabels.add("Thu");
        xAxisLabels.add("Fri");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String keyGreenMax = getString(R.string.key_count_color_threshold_green);
        String keyYellowMax = getString(R.string.key_count_color_threshold_yellow);

        // Get threshold values.
        Double greenMax = new Double(preferences.getString(keyGreenMax, "1.0"));
        Double yellowMax = new Double(preferences.getString(keyYellowMax, "2.0"));
        ArrayList<Integer> colors = new ArrayList<>();

        // Maximum data value.
        Float max = new Float(0);

        // Set bar color based on the current thresholds.
        for (Float value : rawValues) {

            if (value <= greenMax) {
                colors.add(this.getResources().getColor(R.color.issue_green));
            } else if (value <= yellowMax) {
                colors.add(this.getResources().getColor(R.color.issue_yellow));
            } else {
                colors.add(this.getResources().getColor(R.color.issue_red));
            }

            if (value.intValue() > max) {
                max = value;
            }
        }

        // Set of value ArrayList(s).
        // In this case only one set of data with one set of colors.
        BarDataSet barDataSet = new BarDataSet(chartValues, "BarData");
        barDataSet.setColors(colors);

        // Connecting the bar chart to the layout and some chart specific settings.
        BarChart barChart = (BarChart) this.findViewById(R.id.issue_update_quality_detail_landscape_activity_chart);
        barChart.getLegend().setEnabled(false);
        barChart.setDescription("");
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setTextSize(15);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setStartAtZero(true);
        barChart.getAxisLeft().setTextSize(15);
        barChart.getAxisLeft().setLabelCount(new Double(Math.ceil(max)).intValue() + 1, true);
        barChart.getAxisLeft().setAxisMaxValue(new Double(Math.ceil(max)).intValue());
        barChart.getAxisLeft().setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return String.format("%.0f", value);
            }
        });

        BarData barData = new BarData(xAxisLabels, barDataSet);

        // Actual bar chart data (x axis labels (String) and data set (array(s))
        barData.setHighlightEnabled(false);
        barData.setDrawValues(false);

        // Set data and redraw bar chart
        barChart.setData(barData);
        barChart.invalidate();
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

    // Starts new developer detail activity.
    // This is necessary since it is the same context but different data to show.
    // If it were the same data a landscape layout would have been sufficient.
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Intent intent = new Intent(this, IssueUpdateQualityDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            intent.putExtra(getString(R.string.key_intent_message), this.issueKey);

            startActivity(intent);
        }
    }
}
