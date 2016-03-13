package de.htwg.tqm.app.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.issueUpdateQuality.IssueUpdateQualityOverviewActivity;
import de.htwg.tqm.app.volley.ViewedIssuesHandler;
import de.htwg.tqm.app.volley.JiraApi;
import de.htwg.tqm.app.volley.JiraApiFactory;
import de.htwg.tqm.app.model.JiraIssue;

import java.util.Map.Entry;
import java.util.Set;

/**
 * Retrieves critical issues.
 */
public final class CriticalIssuesFetchService extends IntentService {

    private JiraApi api;
    private String projectKey;

    private final Listener<JiraIssue[]> issuesListener;
    private final ErrorListener errorListener;

    public CriticalIssuesFetchService() {
        super("Jira Issues Fetch Service");

        errorListener = new NotificationIssuesErrorListener();
        issuesListener = new NotificationIssuesListener();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(CriticalIssuesFetchService.class.getSimpleName(),
                "onHandleIntent called.");

        setupInstanceVariablesFromPrefs();
        loadIssues();
    }

    private void setupInstanceVariablesFromPrefs() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        api = JiraApiFactory.getInstance(this);
        projectKey = prefs.getString(getString(R.string.key_project), "none");
    }

    private void loadIssues() {
        api.getAssignedIssuess(projectKey, issuesListener,
                errorListener);
    }

    private void sendNotification(Set<Entry<String, Double>> newIssues) {
        final PendingIntent startAppIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, IssueUpdateQualityOverviewActivity.class), 0);

        final String issuesList = getIssuesNotificationList(newIssues);

        final Notification notifi = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_description))
                .setContentIntent(startAppIntent)
                .setStyle(
                        new NotificationCompat.BigTextStyle()
                                .bigText(issuesList))
                .setSmallIcon(R.drawable.ic_launcher).build();
        final NotificationManager manager = (NotificationManager) getSystemService(IntentService.NOTIFICATION_SERVICE);

        // hide the notification after it is selected
        notifi.flags |= Notification.FLAG_AUTO_CANCEL;

        manager.notify(0, notifi);
        Log.i(CriticalIssuesFetchService.class.getSimpleName(),
                "Notification sent.");
    }

    private String getIssuesNotificationList(
            Set<Entry<String, Double>> newIssues) {
        final StringBuilder sb = new StringBuilder();

        for (final Entry<String, Double> issue : newIssues) {
            sb.append(issue.getKey()).append(" | ");
        }
        sb.replace(sb.length() - 3, sb.length(), "");
        return sb.toString();
    }

    /**
     * Checks if any new relevant issues are available. If yes, then send a
     * notification.
     */
    private class NotificationIssuesListener implements Listener<JiraIssue[]> {

        @Override
        public void onResponse(JiraIssue[] issues) {
            final Context context = CriticalIssuesFetchService.this;
            final boolean allRelevantIssuesSeen = ViewedIssuesHandler
                    .allRelevantIssuesSeen(context, issues);

            if (!allRelevantIssuesSeen) {
                final Set<Entry<String, Double>> newIssues = ViewedIssuesHandler
                        .getRelevantIssues(CriticalIssuesFetchService.this,
                                issues);
                ViewedIssuesHandler.markRelevantIssuesAsSeen(context, issues);
                sendNotification(newIssues);
            } else {
                Log.i(CriticalIssuesFetchService.class.getSimpleName(),
                        "No new critical issues found.");
            }
        }
    }

    /**
     * Handles error, does mainly logging.
     */
    private class NotificationIssuesErrorListener implements ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(NotificationIssuesErrorListener.class.getSimpleName(),
                    error.toString());
        }

    }

}
