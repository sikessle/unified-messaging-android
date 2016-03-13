package de.htwg.tqm.app.volley;

import net.jcip.annotations.ThreadSafe;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.content.Context;

/**
 * Singleton to create a request queue and share it among the application parts.
 * This improves performance by reusing a {@link RequestQueue}
 */
@ThreadSafe
public final class RequestQueueSingleton {
	private static RequestQueue INSTANCE;

	/**
	 * Creates if not already created a new request queue.
	 *
	 * @param context
	 *            The context at which the request queue will be created.
	 * @return A new or existing request queue.
	 */
	synchronized public static RequestQueue init(Context context) {
		if (INSTANCE == null) {
			INSTANCE = Volley.newRequestQueue(context);
		}
		return INSTANCE;
	}

}
