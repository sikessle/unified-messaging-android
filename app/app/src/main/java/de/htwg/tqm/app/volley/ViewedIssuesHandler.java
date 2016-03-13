package de.htwg.tqm.app.volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.jcip.annotations.Immutable;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.model.JiraIssue;
import de.htwg.tqm.app.model.JiraIssueCategory;

/**
 * Takes care of viewed issues (i.e. to notify about new, unseen issues).
 */
@Immutable
public final class ViewedIssuesHandler {

	private static final String STORE_VIEWED_ISSUES_KEY = "de.htwg.ticketqualitymonitor.viewed_issues";

	private ViewedIssuesHandler() {
	}

	public static Set<Entry<String, Double>> getRelevantIssues(Context context,
			JiraIssue[] issues) {
		final double thresholdGreen = getThresholdGreen(context);
		final double thresholdYellow = getThresholdYellow(context);
		final Map<String, Double> criticalIssues = new HashMap<>();
		JiraIssueCategory category;

		for (final JiraIssue issue : issues) {
			category = JiraIssueCategory.fromIssue(issue, thresholdGreen,
					thresholdYellow);
			if (category == JiraIssueCategory.RED) {
				criticalIssues.put(issue.getKey(),
						issue.getHoursPerUpdate());
			}
		}

		return criticalIssues.entrySet();
	}

	private static SharedPreferences getStore(Context context) {
		return context.getSharedPreferences(
				ViewedIssuesHandler.STORE_VIEWED_ISSUES_KEY, 0);
	}

	/**
	 * Clears all seen issues.
	 */
	public static void clearSeenIssues(Context context) {
		getStore(context).edit().clear().apply();
		Log.i(ViewedIssuesHandler.class.getSimpleName(), "Seen issues cleared.");
	}

	/**
	 * Marks the all relevant issues of the given issues as seen.
	 */
	public static void markRelevantIssuesAsSeen(Context context,
			JiraIssue[] issues) {

		final Set<Entry<String, Double>> relevantIssues = getRelevantIssues(
				context, issues);
		final Editor editor = getStore(context).edit();

		for (final Map.Entry<String, Double> entry : relevantIssues) {
			editor.putString(getUniqueKey(entry), ".");
		}

		editor.apply();
		Log.i(ViewedIssuesHandler.class.getSimpleName(),
				"Relevant issues marked as seen.");
	}

	/**
	 * Checks if the store contains all of the relevant issues (from the given
	 * issues).
	 */
	public static boolean allRelevantIssuesSeen(Context context,
			JiraIssue[] issues) {

		final Set<Entry<String, Double>> relevantIssues = getRelevantIssues(
				context, issues);
		final SharedPreferences store = getStore(context);

		for (final Entry<String, Double> entry : relevantIssues) {
			if (!store.contains(getUniqueKey(entry))) {
				return false;
			}
		}
		return true;
	}

	private static String getUniqueKey(Entry<String, Double> entry) {
		return entry.getKey() + entry.getValue();
	}

	/**
	 * Returns the threshold for an issue to be categorized as green.
	 */
	public static double getThresholdGreen(Context context) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return Double.parseDouble(prefs.getString(
				context.getString(R.string.key_quality_color_threshold_green), "2.0"));
	}

	/**
	 * Returns the threshold for an issue to be categorized as yellow.
	 */
	public static double getThresholdYellow(Context context) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return Double.parseDouble(prefs.getString(
				context.getString(R.string.key_quality_color_threshold_yellow), "2.0"));
	}

}
