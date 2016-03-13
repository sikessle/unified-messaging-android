package de.htwg.tqm.app.volley;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.volley.Response.Listener;

import de.htwg.tqm.app.model.JiraIssue;

/**
 * Handles the successful request of issues from the Jira API.
 */
public final class IssuesListener implements Listener<JiraIssue[]> {
	private final ArrayAdapter<JiraIssue> adapter;
	private final SwipeRefreshLayout swipeRefresh;

	/**
	 * @param adapter
	 *            An adapter to fill with the projects.
	 */
	public IssuesListener(ArrayAdapter<JiraIssue> adapter,
			SwipeRefreshLayout swipeRefresh) {
		this.adapter = adapter;
		this.swipeRefresh = swipeRefresh;
	}

	@Override
	public void onResponse(JiraIssue[] issues) {
		adapter.clear();
		adapter.addAll(issues);
		swipeRefresh.setRefreshing(false);

		ViewedIssuesHandler.markRelevantIssuesAsSeen(adapter.getContext(),
				issues);

		Log.i(IssuesListener.class.getSimpleName(), "Issues loaded.");
	}

}