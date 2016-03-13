package de.htwg.tqm.app.volley;

import com.android.volley.Response.Listener;

import de.htwg.tqm.app.model.JiraIssue;

/**
 * Listens for JQL responses.
 */
public final class JqlIssueListener implements Listener<JqlObject> {

	private final Listener<JiraIssue[]> issueListener;

	/**
	 * @param issueListener
	 *            Listener which handles the issues.
	 */
	public JqlIssueListener(Listener<JiraIssue[]> issueListener) {
		this.issueListener = issueListener;
	}

	@Override
	public void onResponse(JqlObject jql) {
		issueListener.onResponse(jql.getIssues());
	}

}