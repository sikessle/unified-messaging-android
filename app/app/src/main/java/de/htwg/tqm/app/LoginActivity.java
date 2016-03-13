package de.htwg.tqm.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

import de.htwg.tqm.app.notification.NotificationServiceManager;
import de.htwg.tqm.app.service.WebSocketService;
import de.htwg.tqm.app.settings.MainPreferenceActivity;
import de.htwg.tqm.app.util.ServerResource;

/*
 * Start activity. Only visible if user name and password are empty.
 */
public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

        // Get username and password from shared preferences.
        final String keyUser = this.getString(R.string.key_jira_username);
        final String keyPass = this.getString(R.string.key_jira_password);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String username = preferences.getString(keyUser, "");
        String password = preferences.getString(keyPass, "");

        // Check if username and password are not empty in order to skip the login activity
        // and start the dashboard activity right away.
        if (username.length() > 0 || password.length() > 0) {
            this.startOverviewActivity();
        }

		// Stores entered data on enter button pressed in password text field.
		EditText editText = (EditText) findViewById(R.id.login_activity_password);
		editText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				boolean handled = false;

		        if (event.getAction() == KeyEvent.ACTION_UP
		        		&& keyCode == KeyEvent.KEYCODE_ENTER) {

		            handled = true;
		            LoginActivity.this.storeUserData(v);
		        }

		        return handled;

			}
		});

        ServerResource.getInstance(this).registerUser();
        NotificationServiceManager.startWebSocketService(this);
	}

    // Store user name and password in shared preferences.
    public void storeUserData(View view) {
        EditText userName = (EditText) findViewById(R.id.login_activity_username);
        EditText password = (EditText) findViewById(R.id.login_activity_password);

        final String keyUser = this.getString(R.string.key_jira_username);
        final String keyPass = this.getString(R.string.key_jira_password);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(keyUser, userName.getText().toString());
        editor.putString(keyPass, password.getText().toString());
        editor.commit();

        this.startOverviewActivity();
    }

    // Start dashboard activity.
    private void startOverviewActivity() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }

    // Settings menu.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here.
		final int id = item.getItemId();
		if (id == R.id.settingsAction) {
			final Intent settingsIntent = new Intent(this,
					MainPreferenceActivity.class);
			startActivity(settingsIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
