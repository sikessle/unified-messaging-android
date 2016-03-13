package de.htwg.tqm.app.communication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.model.Dialog;
import de.htwg.tqm.app.model.Message;
import de.htwg.tqm.app.model.NewMessage;
import de.htwg.tqm.app.model.Resolve;
import de.htwg.tqm.app.util.DataStorage;
import de.htwg.tqm.app.util.SelfUpdatingAdapter;
import de.htwg.tqm.app.util.ServerResource;

public class InboxDialogActivity extends Activity implements SelfUpdatingAdapter {

    private long dialogKey;
    private Dialog dialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inbox_dialog_activity);

        // Get dialog key (key of opened dialog) from intent extra
        Intent intent = getIntent();
        dialogKey = intent.getLongExtra(getString(R.string.key_intent_message), 0);

        Log.i("InboxDialogActivity", "ID: " + this.dialogKey);

        // Get dialog object from data storage
        dialog = DataStorage.getInstance().getDialog(dialogKey);

        if (dialog == null) {
            this.finish();
        }

        this.dialogSetup();
        this.buttonSetup();
        //this.swipeRefreshSetup();
    }

    private void swipeRefreshSetup() {
        /*swipeRefreshLayout = (SwipeRefreshLayout) findViewById(
                R.id.inbox_dialog_activity_swipe_refresh_layout);

        // Listener for pull-down refresh; listens to pull-down-refresh layout
        SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                RequestListener listener = new RequestListener(
                        InboxDialogActivity.this.getApplicationContext(),
                        InboxDialogActivity.this,
                        swipeRefreshLayout);

                ServerResource.getInstance(InboxDialogActivity.this).getUsers(listener);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);*/
    }

    private void dialogSetup() {
        // Set dialog name
        TextView dialogName = (TextView) findViewById(R.id.inbox_activity_dialog_name);
        dialogName.setText(dialog.getSubject());

        // Set movement method in order to make dialog messages scrollable
        TextView history = (TextView) findViewById(R.id.inbox_activity_dialog_history);
        history.setMovementMethod(new ScrollingMovementMethod());

        // Display messages of the dialog
        if (dialog.getMessages().size() > 0) {

            // Get sender of first message of dialog (only for coloring purposes)
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                    InboxDialogActivity.this);

            final String username = preferences.getString(getString(R.string.key_jira_username), "");

            for (Message message : dialog.getMessages()) {

                Log.i("InboxDialogActivity", message.getUser());
                Log.i("InboxDialogActivity", message.getBody());

                // Color message background depending on the sender
                SpannableString messageSpan = new SpannableString(message.getBody());
                int color = 0;

                if (message.getBody().contains(username)) {
                    color = getResources().getColor(R.color.issue_green);
                } else {
                    color = getResources().getColor(R.color.issue_red);
                }

                messageSpan.setSpan(new BackgroundColorSpan(color), 0, message.getBody().length(),
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                history.append(messageSpan);
                history.append(String.format("%n%n"));
            }
        }
    }

    private void buttonSetup() {
        // Set onClick listener for send button
        Button sendButton = (Button) findViewById(R.id.inbox_activity_dialog_send_message);
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                        InboxDialogActivity.this);

                final String username = preferences.getString(getString(R.string.key_jira_username), "");
                final EditText newMessageText = (EditText) findViewById(R.id.inbox_activity_dialog_new_message);
                final NewMessage newMessage = new NewMessage(username, newMessageText.getText().toString());

                ServerResource.getInstance(getApplicationContext()).sendMessage(dialog.getDialogID(), newMessage);
                Toast.makeText(InboxDialogActivity.this, "Message sent", Toast.LENGTH_SHORT).show();

                InboxDialogActivity.this.finish();
            }

        });

        // Set onClick listener for resolve button
        Button resolveButton = (Button) findViewById(R.id.inbox_activity_dialog_resolve_dialog);
        resolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                        InboxDialogActivity.this);

                final String username = preferences.getString(getString(R.string.key_jira_username), "");

                //ServerResource.getInstance(getApplicationContext()).
                Toast.makeText(InboxDialogActivity.this, "Dialog resolved", Toast.LENGTH_SHORT).show();
                ServerResource.getInstance(getApplicationContext()).resolveDialog(dialog.getDialogID(),
                        new Resolve(username));
                InboxDialogActivity.this.finish();
            }
        });
    }

    @Override
    public void newDataAvailable() {
        this.dialogSetup();
    }
}
