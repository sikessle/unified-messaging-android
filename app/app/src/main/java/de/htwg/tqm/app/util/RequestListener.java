package de.htwg.tqm.app.util;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import de.htwg.tqm.app.R;

/**
 * Listener that adapters can pass to updating classes in order to get notified when new data
 * is available and to stop swipe refreshing animation.
 */
public class RequestListener {

    private final Context context;
    private final SelfUpdatingAdapter adapter;
    private final SwipeRefreshLayout swipeRefresh;

    /**
     * @param adapter
     *            An adapter to fill with the projects.
     */
    public RequestListener(Context context, SelfUpdatingAdapter adapter,
                           SwipeRefreshLayout swipeRefresh) {
        this.context = context;
        this.adapter = adapter;
        this.swipeRefresh = swipeRefresh;
    }

    public void onSuccess() {
        adapter.newDataAvailable();

        if (swipeRefresh != null) {
            swipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setRefreshing(false);
                }
            });
        }
    }

    public void onError(final String message) {
        String text = String.format("%s%n%s", message, context.getString(R.string.network_error));
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();

        if (swipeRefresh != null) {
            swipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setRefreshing(false);
                }
            });
        }
    }
}

