package de.htwg.tqm.app.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.htwg.tqm.app.DashboardActivity;
import de.htwg.tqm.app.R;
import de.htwg.tqm.app.communication.DialogLoadingActivity;
import de.htwg.tqm.app.communication.InboxDialogActivity;
import de.htwg.tqm.app.issueAssignment.IssueAssignmentDetailActivity;
import de.htwg.tqm.app.issueUpdateQuality.IssueUpdateQualityDetailActivity;
import de.htwg.tqm.app.model.WebSocketNotification;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by tckeh on 12/9/15.
 */
public class WebSocketResource {

    private static final String TAG = "WEB_SOCKET";

    private Context context;

    private int notificationID = 0;

    public WebSocketHandler handler;

    private static WebSocketResource instance;
    private final WebSocketConnection webSocketConnection = new WebSocketConnection();

    private WebSocketResource(Context context) {
        this.context = context;
    }

    public static WebSocketResource getInstance(Context context) {
        if (instance == null) {
            instance = new WebSocketResource(context);
        }

        return instance;
    }

    public void start() {

        final String webSocketUri = "ws://10.0.2.2:8080/tqm/ws";

        this.handler = new WebSocketHandler() {

            @Override
            public void onOpen() {
                Log.d(TAG, "Status: Connected to " + webSocketUri);

                WebSocketResource.this.attachToPush();
            }

            @Override
            public void onTextMessage(String payload) {

                WebSocketNotification notification = new Gson().fromJson(payload, WebSocketNotification.class);
                Intent resultIntent = new Intent(context, DashboardActivity.class);

                String notificationTitle = "";
                String notificationText= "";

                Log.i(TAG, notification.getType());

                switch (notification.getType()) {
                    case "metricViolation":
                        boolean communicate = notification.getContent().get("communicateWithDeveloper").getAsBoolean();
                        String metricType = notification.getContent().get("violationName").getAsString();

                        if (communicate) {
                            resultIntent = createDialog(notification);
                            notificationTitle = "No response to metric violation";
                            notificationText = notification.getContent().get("developer").getAsString()
                                + " did not respond to a metric violation. Click to start a dialog.";

                        } else if (metricType.equals("updateRateViolated")) {
                            resultIntent = updateRateViolation(notification);
                            notificationTitle = "Update Rate Violation";
                            notificationText = "You did not update your issues regularly. Click to see details.";

                        } else if (metricType.equals("assignedIssuesCountViolated")) {
                            resultIntent = assignmentViolation(notification);
                            notificationTitle = "Issue Assignment Violation";
                            notificationText = "There are too many issues assigned to you. Click to see details.";
                        }
                        break;

                    case "dialogCreated":
                        resultIntent = showDialog(notification);
                        notificationTitle = "Dialog created";
                        notificationText = "A dialog was created. Click here to see it.";
                        break;
                    case "dialogMessageCreated":
                        resultIntent = showDialog(notification);
                        notificationTitle = "New Message";
                        notificationText = "A new message was added to a dialog. Click here to see it.";
                        break;
                    case "missingDialogResponse":
                        resultIntent = showDialog(notification);
                        notificationTitle = "Missing response to dialog";
                        notificationText = "Your answer to a dialog is missing. Click here to see it.";
                        break;
                }

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                WebSocketResource.this.context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(notificationTitle)
                                .setContentText(notificationText)
                                .setContentIntent(resultPendingIntent)
                                .setVibrate(new long[]{500, 500, 500});

                Notification notifi = mBuilder.build();
                notifi.flags |= Notification.FLAG_AUTO_CANCEL;

                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                mNotifyMgr.notify(notificationID++, notifi);

                Log.d(TAG, "Got echo: " + payload);
            }

            @Override
            public void onClose(int code, String reason) {
                Log.d(TAG, "Connection lost.");
            }
        };

        try {
            webSocketConnection.connect(webSocketUri, handler);
        } catch (WebSocketException e) {

            Log.d(TAG, e.toString());
        }
    }

    public void stop() {
        webSocketConnection.disconnect();
    }

    private Intent showDialog(final WebSocketNotification notification) {
        Intent resultIntent = new Intent(context, DialogLoadingActivity.class);

        resultIntent.putExtra(context.getString(R.string.key_intent_boolean), false);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        resultIntent.putExtra(context.getString(R.string.key_intent_message),
                notification.getContent().get("dialogID").getAsLong());

        return resultIntent;
    }

    private Intent createDialog(final WebSocketNotification notification) {
        Intent resultIntent = new Intent(context, DialogLoadingActivity.class);

        resultIntent.putExtra(context.getString(R.string.key_intent_boolean), true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                context.getApplicationContext());
        String username = preferences.getString(context.getString(R.string.key_jira_username), "");

        resultIntent.putExtra(context.getString(R.string.key_intent_create_dialog_initiator),
                username);
        resultIntent.putExtra(context.getString(R.string.key_intent_create_dialog_affected),
                notification.getContent().get("developer").getAsString());

        String subject = "Developer " + notification.getContent().get("developer").getAsString()
                + " violated a metric";

        resultIntent.putExtra(context.getString(R.string.key_intent_create_dialog_subject), subject);
        resultIntent.putExtra(context.getString(R.string.key_intent_create_dialog_violationID),
                notification.getContent().get("violationID").getAsLong());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        return resultIntent;
    }

    private Intent updateRateViolation(final WebSocketNotification notification) {
        Intent resultIntent = new Intent(context, IssueUpdateQualityDetailActivity.class);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        resultIntent.putExtra(context.getString(R.string.key_intent_message),
                notification.getContent().getAsJsonObject("issue").get("key").getAsString());

        return resultIntent;
    }

    private Intent assignmentViolation(final WebSocketNotification notification) {
        Intent resultIntent = new Intent(context, IssueAssignmentDetailActivity.class);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        resultIntent.putExtra(context.getString(R.string.key_intent_message),
                notification.getContent().get("developer").getAsString());

        return resultIntent;
    }

    public void attachToPush() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                context.getApplicationContext());
        String username = preferences.getString(context.getString(R.string.key_jira_username), "");
        String project = preferences.getString(context.getString(R.string.key_project), "");

        JsonObject attachObject = new JsonObject();
        JsonObject contentObject = new JsonObject();

        attachObject.addProperty("type", "attach");
        contentObject.addProperty("user", username);
        contentObject.addProperty("project", project);
        attachObject.add("content", contentObject);

        String message = attachObject.toString();
        Log.i(TAG, message);

        if (this.webSocketConnection.isConnected()) {
            this.webSocketConnection.sendTextMessage(message);
        } else {
            start();
        }
    }
}
