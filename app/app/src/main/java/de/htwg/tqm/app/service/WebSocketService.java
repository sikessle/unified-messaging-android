package de.htwg.tqm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import de.htwg.tqm.app.util.WebSocketResource;

public class WebSocketService extends Service {

    private final IBinder mBinder = new LocalBinder();
    WebSocketResource webSocketResource;

    public WebSocketService() {
        this.webSocketResource = WebSocketResource.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.webSocketResource.start();

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        this.webSocketResource.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    public void attachToPush() {
        this.webSocketResource.attachToPush();
    }
}
