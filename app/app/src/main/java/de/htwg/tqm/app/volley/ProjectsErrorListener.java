package de.htwg.tqm.app.volley;

import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

/**
 * Handles error while retrieving projects from the Jira API.
 */
public final class ProjectsErrorListener implements ErrorListener {
	private final ListPreference prefList;
	private final String errorMessage;
	private final OnPreferenceClickListener errorClickListener;

	/**
	 * @param errorClickListener
	 *            A click listener which handles a click on the preference item
	 *            if the loading failed.
	 * @param prefList
	 *            The list to clear if the loading fails.
	 * @param errorMessage
	 *            An error message to show instead of the list if the loading
	 *            fails.
	 */
	public ProjectsErrorListener(OnPreferenceClickListener errorClickListener,
			ListPreference prefList, String errorMessage) {
		this.errorClickListener = errorClickListener;
		this.prefList = prefList;
		this.errorMessage = errorMessage;
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		// Clear the projects selection list and set a new click listener to
		// start an action when the user clicks the projects item (i.e.
		// reloading the content).
		prefList.setSummary(errorMessage);
		prefList.setEntries(new String[] {});
		prefList.setEntryValues(new String[] {});
		prefList.setOnPreferenceClickListener(errorClickListener);

		Log.e(ProjectsErrorListener.class.getSimpleName(), error.toString());
	}

}