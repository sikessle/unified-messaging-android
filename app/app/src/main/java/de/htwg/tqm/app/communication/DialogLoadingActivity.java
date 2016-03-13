package de.htwg.tqm.app.communication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import de.htwg.tqm.app.DashboardActivity;
import de.htwg.tqm.app.R;
import de.htwg.tqm.app.model.CreateCommunication;
import de.htwg.tqm.app.util.RequestListener;
import de.htwg.tqm.app.util.SelfUpdatingAdapter;
import de.htwg.tqm.app.util.ServerResource;

public class DialogLoadingActivity extends Activity implements SelfUpdatingAdapter {

    private long dialogID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_loading);

        // Get dialog key (key of opened dialog) from intent extra
        Intent intent = getIntent();
        dialogID = intent.getLongExtra(getString(R.string.key_intent_message), 0);
        final boolean startCommunication = intent.getBooleanExtra(getString(R.string.key_intent_boolean), false);

        if (startCommunication) {
            String initiator = intent.getStringExtra(getString(R.string.key_intent_create_dialog_initiator));
            String affected = intent.getStringExtra(getString(R.string.key_intent_create_dialog_affected));
            String subject = intent.getStringExtra(getString(R.string.key_intent_create_dialog_subject));
            long violationID = intent.getLongExtra(getString(R.string.key_intent_create_dialog_violationID), 0);

            CreateCommunication createCommunication = new CreateCommunication(
                    initiator, affected, subject, violationID);

            ServerResource.getInstance(this).createDialog(createCommunication, this, this);
        } else {
            RequestListener listener = new RequestListener(this, this, null);
            ServerResource.getInstance(this).getDialogs(listener);
        }

    }

    @Override
    public void newDataAvailable() {
        Intent resultIntent = new Intent(this, InboxDialogActivity.class);

        Log.i("DialogLoadingActivity", "ID: " + this.dialogID);
        resultIntent.putExtra(getString(R.string.key_intent_message), dialogID);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent dashboardIntent = new Intent(this, DashboardActivity.class);

        TaskStackBuilder.create(this)
                // add all of DetailsActivity's parents to the stack,
                // followed by DetailsActivity itself
                .addParentStack(DashboardActivity.class)
                .addNextIntentWithParentStack(dashboardIntent)
                .addNextIntentWithParentStack(resultIntent)
                .startActivities();
    }

    public void setDialogID(final long id) {
        this.dialogID = id;
        Log.i("DialogLoadingActivity", "ID: " + this.dialogID);
    }
}
