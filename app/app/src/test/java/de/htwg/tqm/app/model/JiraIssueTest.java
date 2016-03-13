package de.htwg.tqm.app.model;

import de.htwg.tqm.app.model.JiraIssue.Fields;
import de.htwg.tqm.app.model.JiraIssue.Timetracking;
import de.htwg.tqm.app.model.JiraIssue.Worklog;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JiraIssueTest {

    @Test
	public void testGetSpentHoursPerUpdate() throws NoSuchFieldException,
			IllegalAccessException, IllegalArgumentException {

		final long timeSpentSeconds = 9600;
		final int totalWorklogItems = 4;
		// Minute precisions
		final double expectedSpentHoursPerUpdate = timeSpentSeconds / 60 / 60.
				/ totalWorklogItems;

		final JiraIssue issue = prepareIssue(timeSpentSeconds,
				totalWorklogItems);

        assertThat(expectedSpentHoursPerUpdate, is(issue.getSpentHoursPerUpdate()));
	}

	private JiraIssue prepareIssue(long timeSpentSeconds, int totalWorklogItems)
			throws NoSuchFieldException, IllegalAccessException,
			IllegalArgumentException {
		final JiraIssue issue = new JiraIssue();
		final JiraIssue.Fields issueFields = prepareFields(timeSpentSeconds,
				totalWorklogItems);
		final Field fieldFields = JiraIssue.class.getDeclaredField("fields");

		fieldFields.setAccessible(true);
		fieldFields.set(issue, issueFields);

		return issue;
	}

	private Fields prepareFields(long timeSpentSeconds, int totalWorklogItems)
			throws NoSuchFieldException, IllegalAccessException,
			IllegalArgumentException {
		final JiraIssue.Fields issueFields = new JiraIssue.Fields();

		final JiraIssue.Timetracking issueTimetracking = prepareTimetracking(timeSpentSeconds);
		final JiraIssue.Worklog issueWorklog = prepareWorklog(totalWorklogItems);

		final Field fieldTimetracking = JiraIssue.Fields.class
				.getDeclaredField("timetracking");
		final Field fieldWorklog = JiraIssue.Fields.class
				.getDeclaredField("worklog");

		fieldTimetracking.setAccessible(true);
		fieldWorklog.setAccessible(true);

		fieldWorklog.set(issueFields, issueWorklog);
		fieldTimetracking.set(issueFields, issueTimetracking);

		return issueFields;
	}

	private Worklog prepareWorklog(int totalWorklogItems)
			throws NoSuchFieldException, IllegalAccessException,
			IllegalArgumentException {
		final JiraIssue.Worklog issueWorklog = new JiraIssue.Worklog();

		final Field fieldTotal = JiraIssue.Worklog.class
				.getDeclaredField("total");

		fieldTotal.setAccessible(true);
		fieldTotal.set(issueWorklog, totalWorklogItems);

		return issueWorklog;
	}

	private Timetracking prepareTimetracking(long timeSpentSeconds)
			throws NoSuchFieldException, IllegalAccessException,
			IllegalArgumentException {
		final JiraIssue.Timetracking issueTimetracking = new JiraIssue.Timetracking();

		final Field fieldTimeSpentSeconds = JiraIssue.Timetracking.class
				.getDeclaredField("timeSpentSeconds");

		fieldTimeSpentSeconds.setAccessible(true);
		fieldTimeSpentSeconds.set(issueTimetracking, timeSpentSeconds);

		return issueTimetracking;
	}
}