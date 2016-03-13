package de.htwg.tqm.app.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Dispays the main preferences screen.
 */
public final class MainPreferenceActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new MainPreferenceFragment())
		.commit();
	}

}