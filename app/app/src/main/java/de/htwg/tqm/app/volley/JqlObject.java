package de.htwg.tqm.app.volley;

import java.util.Arrays;

import de.htwg.tqm.app.model.JiraIssue;

/**
 * Wrapper type to handle JQL results.
 */
public final class JqlObject {
	private JiraIssue[] issues;

	public JqlObject() {
	}

	public JiraIssue[] getIssues() {
		return Arrays.copyOf(issues, issues.length);
	}

	public void setIssues(JiraIssue[] issues) {
		this.issues = Arrays.copyOf(issues, issues.length);
	}
}
