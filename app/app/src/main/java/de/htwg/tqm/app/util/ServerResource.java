package de.htwg.tqm.app.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.List;

import de.htwg.tqm.app.R;
import de.htwg.tqm.app.communication.DialogLoadingActivity;
import de.htwg.tqm.app.model.CreateCommunication;
import de.htwg.tqm.app.model.DialogResponse;
import de.htwg.tqm.app.model.EmptyResponse;
import de.htwg.tqm.app.model.JiraUser;
import de.htwg.tqm.app.model.Dialog;
import de.htwg.tqm.app.model.JiraIssue;
import de.htwg.tqm.app.model.JiraProject;
import de.htwg.tqm.app.model.NewMessage;
import de.htwg.tqm.app.model.Registration;
import de.htwg.tqm.app.model.Resolve;
import de.htwg.tqm.app.service.WebSocketService;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Client side server representation.
 */
public class ServerResource {

    private static final String TAG = "SERVER_RESOURCE";

    private static ServerResource instance;

    private Intent webSocketService;

    private DataStorage dataStorage;
    private IssueService issueService;
    private Context context;
    private SharedPreferences preferences;

    private ServerResource(final Context context) {
        this.dataStorage = DataStorage.getInstance();

        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        final String userName = preferences.getString(context.getString(
                R.string.key_jira_username), "");
        final String password = preferences.getString(context.getString(
                R.string.key_jira_password), "");

        this.issueService = ServiceFactory.createService(IssueService.class, userName, password);
    }

    public static ServerResource getInstance(final Context context) {
        if (instance == null) {
            instance = new ServerResource(context);
        }

        return instance;
    }

    public Intent getWebSocketService() {
        return this.webSocketService;
    }

    public void setWebSocketService(final Intent webSocketService) {
        if (this.webSocketService == null) {
            this.webSocketService = webSocketService;
        }
    }

    public void registerUser() {
        final String userName = preferences.getString(context.getString(
                R.string.key_jira_username), "");
        String projectName = preferences.getString(context.getString(
                R.string.key_project), "");
        final String role = preferences.getString(context.getString(
                R.string.key_user_role), "");

        Call<EmptyResponse> call = this.issueService.registerClient(new Registration(userName, projectName, role));

        call.enqueue(new Callback<EmptyResponse>() {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    Toast.makeText(ServerResource.this.context,
                            ServerResource.this.context.getString(R.string.register_user_push),
                            Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(ServerResource.this.context,
                            ServerResource.this.context.getString(R.string.network_error),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void getProjects(final RequestListener listener) {
        Call<List<JiraProject>> call = this.issueService.getProjects();

        call.enqueue(new Callback<List<JiraProject>>() {
            @Override
            public void onResponse(Response<List<JiraProject>> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    final List<JiraProject> projects = response.body();
                    ServerResource.this.dataStorage.resetProjects();
                    ServerResource.this.dataStorage.addProjects(projects);

                    listener.onSuccess();

                } else {
                    listener.onError(response.message());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void getIssues(final RequestListener listener) {
        final String projectKey = this.preferences.getString(this.context.getString(
                R.string.key_project), "");
        Call<List<JiraIssue>> call = this.issueService.getIssues(projectKey);

        call.enqueue(new Callback<List<JiraIssue>>() {
            @Override
            public void onResponse(Response<List<JiraIssue>> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    final List<JiraIssue> issues = response.body();
                    ServerResource.this.dataStorage.resetIssues();
                    ServerResource.this.dataStorage.addIssues(new HashSet<>(issues));

                } else {
                    listener.onError(response.message());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void getUsers(final RequestListener listener) {
        final String projectKey = this.preferences.getString(this.context.getString(
                                  R.string.key_project), "");

        Call<List<JiraUser>> call = this.issueService.getUsers(projectKey);

        call.enqueue(new Callback<List<JiraUser>>() {
            @Override
            public void onResponse(Response<List<JiraUser>> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    final List<JiraUser> users = response.body();
                    ServerResource.this.dataStorage.resetDevelopers();
                    ServerResource.this.dataStorage.addDevelopers(new HashSet<>(users));
                    listener.onSuccess();

                } else {
                    listener.onError(response.message());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void getDialog(final RequestListener listener, final long dialogID) {
        Call<Dialog> call = this.issueService.getDialog(dialogID);

        call.enqueue(new Callback<Dialog>() {
            @Override
            public void onResponse(Response<Dialog> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    final Dialog dialog = response.body();
                    ServerResource.this.dataStorage.addDialog(dialog);
                    listener.onSuccess();

                } else {
                    listener.onError(response.message());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void getDialogs(final RequestListener listener) {
        final String userName = preferences.getString(context.getString(
                R.string.key_jira_username), "");
        Call<List<Dialog>> call = this.issueService.getUserDialogs(userName);

        call.enqueue(new Callback<List<Dialog>>() {
            @Override
            public void onResponse(Response<List<Dialog>> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    final List<Dialog> dialogs = response.body();

                    for (Dialog dialog : dialogs) {
                        Log.i(TAG, "Added: " + dialog.getDialogID());
                    }

                    ServerResource.this.dataStorage.resetDialogs();
                    ServerResource.this.dataStorage.addDialogs(dialogs);
                    listener.onSuccess();

                } else {
                    listener.onError(response.message());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void createDialog(final CreateCommunication createCommunication,
                             final SelfUpdatingAdapter adapter,
                             final DialogLoadingActivity activity) {

        Call<DialogResponse> call = this.issueService.createDialog(createCommunication);

        call.enqueue(new Callback<DialogResponse>() {
            @Override
            public void onResponse(Response<DialogResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    activity.setDialogID(response.body().getDialogID());
                    Log.i("ServerResource", "ID: " + response.body().getDialogID());
                    Log.i(TAG, Long.toString(response.body().getDialogID()));
                    ServerResource.this.getDialogs(new RequestListener(null, adapter, null));
                    Log.i(TAG, "Dialog created.");

                } else {
                    Log.e(TAG, "Dialog creation failed!");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void sendMessage(final long dialogID, NewMessage message) {
        Call<EmptyResponse> call = this.issueService.addMessageToDialog(dialogID, message);

        call.enqueue(new Callback<EmptyResponse>() {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    Log.i(TAG, "Message sent.");

                } else {
                    Log.e(TAG, "Sending of message failed!");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void resolveDialog(final long dialogID, final Resolve resolve) {
        Call<EmptyResponse> call = this.issueService.resolveDialog(dialogID, resolve);

        call.enqueue(new Callback<EmptyResponse>() {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    Log.i(TAG, "Dialog marked as resolved.");

                } else {
                    Log.e(TAG, "Marking dialog as resolved failed!");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void deregisterUser(final String user) {
        Call<EmptyResponse> call = this.issueService.unregisterClient(user);

        call.enqueue(new Callback<EmptyResponse>() {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    Log.i(TAG, "User unregistered.");

                } else {
                    Log.e(TAG, "User unregister failed!");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }
}
