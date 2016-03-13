package de.htwg.tqm.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.htwg.tqm.app.notification.NotificationServiceManager;
import de.htwg.tqm.app.util.ServerResource;

public final class OnBootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(OnBootCompletedReceiver.class.getSimpleName(),
                "Device booted. Setting notification service state.");
		//NotificationServiceManager.startOrStopBasedOnPreference(context);

        NotificationServiceManager.startWebSocketService(context);
	}

}
