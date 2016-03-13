package de.htwg.tqm.app.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.model.JiraProject;
import de.htwg.tqm.app.notification.NotificationServiceManager;
import de.htwg.tqm.app.service.WebSocketService;
import de.htwg.tqm.app.util.DataStorage;
import de.htwg.tqm.app.util.RequestListener;
import de.htwg.tqm.app.util.SelfUpdatingAdapter;
import de.htwg.tqm.app.util.ServerResource;
import de.htwg.tqm.app.util.WebSocketResource;
import de.htwg.tqm.app.volley.ViewedIssuesHandler;

/**
 * This fragment shows the main preferences and handles the interaction with
 * items.
 */
public final class MainPreferenceFragment extends PreferenceFragment implements
		OnPreferenceClickListener, OnSharedPreferenceChangeListener, SelfUpdatingAdapter {

    private boolean mIsBound;
    private WebSocketService mBoundService;
    private String oldUserName = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		initProjectList();

        final String userName = getPrefs().getString(getActivity().getString(
                R.string.key_jira_username), "");
        this.oldUserName = userName;

		PreferenceManager.getDefaultSharedPreferences(getActivity())
				.registerOnSharedPreferenceChangeListener(this);

        this.doBindService();
	}

	private void initProjectList() {

        // Fill the projects into the preference list.
        ServerResource.getInstance(this.getActivity().getApplicationContext()).getProjects(
                new RequestListener(this.getActivity().getApplicationContext(), this, null));
	}

	@Override
	public void onResume() {
		super.onResume();
		getPrefs().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPrefs().unregisterOnSharedPreferenceChangeListener(this);
	}

	private SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		final String keyUri = getString(R.string.key_jira_host);
		final String keyUser = getString(R.string.key_jira_username);
		final String keyPass = getString(R.string.key_jira_password);
        final String keyRole = getString(R.string.key_user_role);
		final String keyProject = getString(R.string.key_project);
		final String keyQualityThresholdGreen = getString(R.string.key_quality_color_threshold_green);
		final String keyQualityThresholdYellow = getString(R.string.key_quality_color_threshold_yellow);
        final String keyAssignmentThresholdGreen = getString(R.string.key_count_color_threshold_green);
        final String keyAssignmentThresholdYellow = getString(R.string.key_count_color_threshold_yellow);
		final String keyEnableNotifications = getString(R.string.key_enable_notifications);
		final String keyNotificationsInterval = getString(R.string.key_notifications_interval);

		if (keyUri.equals(key) || keyUser.equals(key) || keyPass.equals(key)) {
			initProjectList();
            ServerResource.getInstance(this.getActivity()).deregisterUser(this.oldUserName);
            ServerResource.getInstance(this.getActivity()).registerUser();
            this.mBoundService.attachToPush();
            final String userName = getPrefs().getString(getActivity().getString(
                    R.string.key_jira_username), "");
            this.oldUserName = userName;
		}

		if (keyQualityThresholdGreen.equals(key) || keyQualityThresholdYellow.equals(key)
				|| keyProject.equals(key)) {
			ViewedIssuesHandler.clearSeenIssues(getActivity());
		}

        if (keyAssignmentThresholdGreen.equals(key) || keyAssignmentThresholdYellow.equals(key)
                || keyProject.equals(key)) {
            ViewedIssuesHandler.clearSeenIssues(getActivity());
        }

		if (keyEnableNotifications.equals(key)) {
			NotificationServiceManager
					.startOrStopBasedOnPreference(getActivity());
		}

		if (keyNotificationsInterval.equals(key)) {
			// Restart service
			NotificationServiceManager.stop(getActivity());
			NotificationServiceManager.start(getActivity());
		}

		if (keyProject.equals(key)) {
			final Preference pref = findPreference(key);
			final ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
            this.mBoundService.attachToPush();
		}

        if (keyRole.equals(key)) {
            final Preference pref = findPreference(key);
            final ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());

            String message1 =
                    "{" +
                    "    type: 'metricViolation'," +
                    "    content: {" +
                    "        developer: 'tokeh'," +
                    "        violationName: 'assignedIssuesCountViolated'," +
                    "        violationID: 506," +
                    "        communicateWithDeveloper: true," +
                    "        issues: [" +
                    "            {" +
                    "                key: 'AUMEWT-78'," +
                    "                name: 'Something-78'" +
                    "            }," +
                    "            {" +
                    "                key: 'AUMEWT-174'," +
                    "                name: 'Something-174'" +
                    "            }" +
                    "        ]," +
                    "        count: 2" +
                    "    }" +
                    "}";

            String message2 =
                    "{" +
                    "    type: 'metricViolation'," +
                    "    content: {" +
                    "        developer: 'tokeh'," +
                    "        violationName: 'updateRateViolated'," +
                    "        violationID: 505," +
                    "        communicateWithDeveloper: false," +
                    "        issue: {" +
                    "            key: 'AUMEWT-114'," +
                    "            name: 'This is order 66'," +
                    "            hoursPerUpdate: 2.3" +
                    "        }" +
                    "    }" +
                    "}";

            String message3 =
                    "{" +
                    "    type: 'dialogCreated'," +
                    "    content: {" +
                    "        dialogID: 1" +
                    "    }" +
                    "}";

            String message4 =
                    "{" +
                    "    type: 'missingDialogResponse'," +
                    "    content: {" +
                    "        dialogID: 1" +
                    "    }" +
                    "}";

            String message5 =
                    "{" +
                            "    type: 'dialogMessageCreated'," +
                            "    content: {" +
                            "        dialogID: 1" +
                            "    }" +
                            "}";

            //WebSocketResource.getInstance(this.getActivity()).handler.onTextMessage(message1);
        }
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// Kill the dialog and reload the projects (retry of failed load)
		((ListPreference) preference).getDialog().dismiss();
        //initProjectList();
		return false;
	}

    @Override
    public void newDataAvailable() {
        ArrayList<JiraProject> projects = DataStorage.getInstance().getProjects();
        final String entries[] = new String[projects.size()];
        final String entryValues[] = new String[projects.size()];

        for (int i = 0; i < projects.size(); i++) {
            entries[i] = projects.get(i).getName();
            entryValues[i] = projects.get(i).getKey();
        }

        final String keyProjectList = getString(R.string.key_project);
        final ListPreference prefList = (ListPreference) findPreference(keyProjectList);

        prefList.setEntries(entries);
        prefList.setEntryValues(entryValues);
        prefList.setSummary(prefList.getEntry());

        Log.e("PREF_PROJECT_LIST", " LOADED!");
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((WebSocketService.LocalBinder)service).getService();
            mBoundService.attachToPush();

            Log.i("MainPreferenceFragment", "Service connected.");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        this.getActivity().bindService(ServerResource.getInstance(
                this.getActivity()).getWebSocketService(), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            this.getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}