package de.htwg.tqm.app.model;

import de.htwg.tqm.app.model.JiraIssue.Fields;
import de.htwg.tqm.app.model.JiraIssue.Timetracking;
import de.htwg.tqm.app.model.JiraIssue.Worklog;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JiraIssueCategoryTest {

	private static final double thresholdGreen = 2.0;
	private static final double thresholdYellow = 4.0;

	@Test
	public void testGreenCategory() {
		testCategory(JiraIssueCategory.GREEN, 1);
		testCategory(JiraIssueCategory.GREEN, 2);
	}

	@Test
	public void testYellowCategory() {
		testCategory(JiraIssueCategory.YELLOW, 3);
		testCategory(JiraIssueCategory.YELLOW, 4);
	}

	@Test
	public void testRedCategory() {
		testCategory(JiraIssueCategory.RED, 5);
	}

	private void testCategory(JiraIssueCategory expectedCategory,
			int spendHoursPerUpdate) {
		final JiraIssue issue = createIssue(spendHoursPerUpdate);
		final JiraIssueCategory actualCategory = JiraIssueCategory.fromIssue(
				issue, thresholdGreen, thresholdYellow);
		assertEquals(expectedCategory, actualCategory);
	}

	private JiraIssue createIssue(int spentHoursPerUpdate) {
		final Timetracking timetracking = new Timetracking();
		final Worklog worklog = new Worklog();

		timetracking.setTimeSpentSeconds(spentHoursPerUpdate * 60 * 60);
		worklog.setTotal(1);

		final JiraIssue issue = new JiraIssue();
		final Fields fields = new Fields();
		fields.setTimetracking(timetracking);
		fields.setWorklog(worklog);
		issue.setFields(fields);

		return issue;
	}
}