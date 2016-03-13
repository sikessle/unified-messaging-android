package de.htwg.tqm.app.issueUpdateQuality;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.settings.MainPreferenceActivity;
import de.htwg.tqm.app.util.DataStorage;
import de.htwg.tqm.app.model.JiraIssue;
import de.htwg.tqm.app.util.RequestListener;
import de.htwg.tqm.app.util.SelfUpdatingAdapter;
import de.htwg.tqm.app.util.ServerResource;

/**
 * Starting activity which display a list of assigned issues and their remaining
 * costs update rate.
 */
public final class IssueUpdateQualityOverviewActivity extends Activity {

	private ArrayAdapter<JiraIssue> adapter;
    private ListView issuesList;
	private SwipeRefreshLayout swipeRefresh;
    private ArrayList<JiraIssue> values;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.issue_update_quality_overview_activity);

        this.values = DataStorage.getInstance().getIssues();
		swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.issue_update_quality_overview_activity_swipe_layout);
        //swipeRefresh.setOnRefreshListener(this);
		adapter = new IssueUpdateQualityListAdapter(this, this.values);
		issuesList = ((ListView) findViewById(R.id.issue_update_quality_overview_activity_list_view));
		issuesList.setAdapter(adapter);

        issuesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JiraIssue issue = IssueUpdateQualityOverviewActivity.this.values.get(position);

                Intent intent = new Intent(IssueUpdateQualityOverviewActivity.this,
                        IssueUpdateQualityDetailActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra(getString(R.string.key_intent_message), issue.getKey());

                startActivity(intent);
            }
        });

        this.swipeRefreshSetup();
	}

    private void swipeRefreshSetup() {
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(
                R.id.issue_update_quality_overview_activity_swipe_layout);

        // Listener for pull-down refresh; listens to pull-down-refresh layout
        OnRefreshListener listener = new OnRefreshListener() {

            @Override
            public void onRefresh() {
                RequestListener listener = new RequestListener(IssueUpdateQualityOverviewActivity.this,
                        (SelfUpdatingAdapter) IssueUpdateQualityOverviewActivity.this.adapter,
                        swipeRefreshLayout);

                ServerResource.getInstance(IssueUpdateQualityOverviewActivity.this).getIssues(listener);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

	/*@Override
	public void onRefresh() {
		RequestListener listener = new RequestListener(IssueUpdateQualityOverviewActivity.this,
                (SelfUpdatingAdapter) this.adapter,	this.swipeRefresh);

        ServerResource.getInstance(this).getIssues(listener);
	}*/

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