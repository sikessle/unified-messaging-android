package de.htwg.tqm.app.util;

import java.util.List;

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
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by tckeh on 12/8/15.
 */
public interface IssueService {

    static final String API_BASE = "/tqm/rest";

    // Clients
    @POST(API_BASE + "/clients")
    Call<EmptyResponse> registerClient(@Body Registration registration);

    @DELETE(API_BASE + "/clients/{name}")
    Call<EmptyResponse> unregisterClient(@Path("name") String name);

    // Projects
    @GET(API_BASE + "/metrics/jira/projects")
    Call<List<JiraProject>> getProjects();

    // Issues
    @GET(API_BASE + "/metrics/jira/projects/{projectKey}/issues")
    Call<List<JiraIssue>> getIssues(@Path("projectKey") String projectKey);

    // Users
    @GET(API_BASE + "/metrics/jira/projects/{projectKey}/users")
    Call<List<JiraUser>> getUsers(@Path("projectKey") String projectKey);

    // Communication
    @POST(API_BASE + "/dialogs")
    Call<DialogResponse> createDialog(@Body CreateCommunication createCommunication);

    @GET(API_BASE + "/dialogs/users/{userName}")
    Call<List<Dialog>> getUserDialogs(@Path("userName") String userName);

    @GET(API_BASE + "/dialogs/{dialogID}")
    Call<Dialog> getDialog(@Path("dialogID") long dialogID);

    @POST(API_BASE + "/dialogs/{dialogID}")
    Call<EmptyResponse> addMessageToDialog(@Path("dialogID") long dialogID, @Body NewMessage newMessage);

    @POST(API_BASE + "/dialogs/{dialogID}/resolve")
    Call<EmptyResponse> resolveDialog(@Path("dialogID") long dialogID, @Body Resolve resolve);
}
