package de.htwg.tqm.app.model;

import com.android.volley.Response.Listener;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import de.htwg.tqm.app.volley.JqlIssueListener;
import de.htwg.tqm.app.volley.JqlObject;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JqlIssueListenerTest {

	@Test
	public void testGetIssues() throws NoSuchFieldException,
			IllegalAccessException, IllegalArgumentException {
		final JqlObject expectedJqlObject = new JqlObject();
		final JiraIssue[] expectedIssues = new JiraIssue[] {};

		expectedJqlObject.setIssues(expectedIssues);

		final TestListener wrappedListener = new TestListener(expectedIssues);
		final JqlIssueListener listener = new JqlIssueListener(wrappedListener);

		listener.onResponse(expectedJqlObject);
	}

	public static class TestListener implements Listener<JiraIssue[]> {

		private final List<JiraIssue> expectedIssues;

		public TestListener(JiraIssue[] expectedIssues) {
			this.expectedIssues = Arrays.asList(expectedIssues);
		}

		@Override
		public void onResponse(JiraIssue[] issues) {
			final List<JiraIssue> actualIssues = Arrays.asList(issues);

			assertThat(expectedIssues, is(actualIssues));
		}

	}
}