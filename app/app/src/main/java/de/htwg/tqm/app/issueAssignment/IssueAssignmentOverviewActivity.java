package de.htwg.tqm.app.issueAssignment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.model.JiraUser;
import de.htwg.tqm.app.settings.MainPreferenceActivity;
import de.htwg.tqm.app.util.SelfUpdatingAdapter;
import de.htwg.tqm.app.util.DataStorage;
import de.htwg.tqm.app.util.RequestListener;
import de.htwg.tqm.app.util.ServerResource;

/*
 * List of all developers and their corresponding count of open issues
 */
public class IssueAssignmentOverviewActivity extends Activity {

    ArrayList<JiraUser> values;
    ArrayAdapter<JiraUser> adapter;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.setContentView(R.layout.issue_assignment_overview_activity);

        this.adapterSetup();
        this.swipeRefreshSetup();
	}

    private void adapterSetup() {
        this.values = DataStorage.getInstance().getUsers();
        this.adapter = new IssueAssignmentOverviewListAdapter(this, this.values);

        ListView listView = (ListView) findViewById(R.id.issue_assignment_overview_activity_list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JiraUser jiraUser = IssueAssignmentOverviewActivity.this.values.get(position);

                Intent intent = new Intent(IssueAssignmentOverviewActivity.this,
                        IssueAssignmentDetailActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra(getString(R.string.key_intent_message), jiraUser.getName());

                startActivity(intent);
            }
        });
    }

    private void swipeRefreshSetup() {
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(
                R.id.issue_assignment_overview_swipe_refresh_layout);

        // Listener for pull-down refresh; listens to pull-down-refresh layout
        OnRefreshListener listener = new OnRefreshListener() {

            @Override
            public void onRefresh() {
                RequestListener listener = new RequestListener(IssueAssignmentOverviewActivity.this,
                        (SelfUpdatingAdapter) IssueAssignmentOverviewActivity.this.adapter,
                        swipeRefreshLayout);

                ServerResource.getInstance(IssueAssignmentOverviewActivity.this).getUsers(listener);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

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
