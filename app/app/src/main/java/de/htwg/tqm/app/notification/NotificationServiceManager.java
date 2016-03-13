package de.htwg.tqm.app.notification;

import java.util.concurrent.TimeUnit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.service.CriticalIssuesFetchService;
import de.htwg.tqm.app.service.WebSocketService;
import de.htwg.tqm.app.util.ServerResource;

public final class NotificationServiceManager {

	private NotificationServiceManager() {
	}

	private static final Class<? extends Service> serviceClass = CriticalIssuesFetchService.class;

	public static void startWebSocketService(Context context) {
        Intent service = new Intent(context, WebSocketService.class);
        context.startService(service);
        ServerResource.getInstance(context).setWebSocketService(service);
	}

	/**
	 * Starts or stops the service based on the users preference.
	 */
	public static void startOrStopBasedOnPreference(Context context) {
		final boolean start = notificationsEnabled(context);

		if (start) {
			start(context);
		} else {
			stop(context);
		}
	}

	private static boolean notificationsEnabled(Context context) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean(
				context.getString(R.string.key_enable_notifications), false);
	}

	/**
	 * Starts the service.
	 */
	public static void start(Context context) {
		final Intent serviceIntent = new Intent(context, serviceClass);
		final PendingIntent pendingIntent = PendingIntent.getService(context,
				0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		final long intervalMillis = getIntervalMillis(context);

		final long firstStart = SystemClock.elapsedRealtime() + intervalMillis;

		getAM(context).setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				firstStart, intervalMillis, pendingIntent);

		Log.i(NotificationServiceManager.class.getSimpleName(),
				"Notification service started.");
	}

	private static long getIntervalMillis(Context context) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		final int intervalMinutes = Integer.parseInt(prefs.getString(
				context.getString(R.string.key_notifications_interval), "1"));

		return TimeUnit.MINUTES.toMillis(intervalMinutes);
	}

	/**
	 * Stops the service.
	 */
	public static void stop(Context context) {
		final Intent serviceIntent = new Intent(context, serviceClass);
		final PendingIntent pendingIntent = PendingIntent.getService(context,
				0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		getAM(context).cancel(pendingIntent);

		Log.i(NotificationServiceManager.class.getSimpleName(),
				"Notification service cancelled.");
	}

	private static AlarmManager getAM(Context context) {
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

}
