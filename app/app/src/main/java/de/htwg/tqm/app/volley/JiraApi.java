package de.htwg.tqm.app.volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;
import android.util.Base64;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;

import de.htwg.tqm.app.model.JiraIssue;
import de.htwg.tqm.app.model.JiraProject;

/**
 * Encapsulates the Jira API and allows some specific calls to it.
 */
@ThreadSafe
public final class JiraApi {

	/** Suffix for the base URI to access the REST API */
	private static final String URI_API_SUFFIX = "rest/api/2/";
	/** Resource key (URI component) to access the projects resource */
	private static final String PROJECTS = "project";
	/** Char as a placeholder */
	private static final String REPLACE_CHAR = "#";
	/** Replace # with a project key. */
	private static final String ISSUES_TEMPLATE = "search?jql=project="
			+ REPLACE_CHAR
			+ "%20AND%20%28status=%22In%20Progress%22%20OR%20status=%22Done%22%20OR%20status=%22Closed%22%20OR%20status=%22Resolved%22%29"
			+ "%20AND%20assignee%20is%20not%20EMPTY"
			+ "&fields=assignee,worklog,timetracking&maxResults=100";
	/** Timeout after which the request fails */
	private static final int TIMEOUT_MS = 4000;
	/** Number of retries before the request fails */
	private static final int MAX_RETRIES = 0;

	private final String uri;
	private final String user;
	private final String pass;
	private final RequestQueue requestQueue;
	private final Map<String, String> credentials;
	private final RetryPolicy retryPolicy;

	/**
	 * @param uri
	 *            The base URI at which the Jira server is accessible.
	 * @param user
	 *            The username to access the Jira server.
	 * @param pass
	 *            The password to access the Jira server.
	 * @param requestQueue
	 *            A {@link RequestQueue} which handles the request sent by this
	 *            object.
	 */
	public JiraApi(String uri, String user, String pass,
			RequestQueue requestQueue) {

		this.uri = completeUri(uri);
		this.user = user;
		this.pass = pass;
		this.requestQueue = requestQueue;
		credentials = new HashMap<>();
		initBasicAuthHeader();
		retryPolicy = new DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

		Log.i(JiraApi.class.getSimpleName(), "Connection data: " + this.user
				+ ":" + this.pass + "@" + this.uri);
	}

	private void initBasicAuthHeader() {
		final String userPass = (user + ":" + pass);
		String userPassBase64;
		try {
			userPassBase64 = Base64.encodeToString(userPass.getBytes("UTF-8"),
					Base64.NO_WRAP);
		} catch (final UnsupportedEncodingException e) {
			Log.e(JiraApi.class.getSimpleName(), e.getMessage());
			throw new RuntimeException(e);
		}
		credentials.put("Authorization", "Basic " + userPassBase64);
	}

	/**
	 * Ensures that the given URI starts with http:// and ends with the URI
	 * suffix to access the REST API.
	 *
	 * @param possibleUri
	 *            The URI to check and sanitize.
	 * @return The completed and valid URI.
	 */
	private String completeUri(String possibleUri) {
		final StringBuilder result = new StringBuilder(possibleUri);
		final String prefix = "http://";
		final String suffix = "/";

		if (!possibleUri.startsWith(prefix)) {
			result.insert(0, prefix);
		}

		if (!possibleUri.endsWith(suffix)) {
			result.append(suffix);
		}

		result.append(URI_API_SUFFIX);
		return result.toString();
	}

	public String getUri() {
		return uri;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

	/**
	 * Creates a base request with some basic headers (like authentication)
	 * which can be furhter complemented.
	 *
	 * @param resource
	 *            The resource to aquire: i.e. "project". The base uri will be
	 *            prepended.
	 * @return The base request for the given resource.
	 */
	protected <T> GsonRequest<T> createBaseRequest(String resource,
			Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
		Map<String, String> headers;
		synchronized (credentials) {
			headers = new HashMap<>(credentials);
		}
		final GsonRequest<T> request = new GsonRequest<>(uri + resource,
				clazz, headers, listener, errorListener);
		request.setRetryPolicy(retryPolicy);

		Log.i(JiraApi.class.getSimpleName(),
				"Request created: " + request.getUrl());

		return request;
	}

	/**
	 * Returns a list of all projects which are visible for the specified user.
	 *
	 * @param listener
	 *            A listener which will be called if the request succeeds to
	 *            handle the result.
	 * @param errorListener
	 *            A listener which will be called if an error occurs.
	 */
	public void getProjects(Listener<JiraProject[]> listener,
			ErrorListener errorListener) {
		final GsonRequest<JiraProject[]> req = createBaseRequest(PROJECTS,
				JiraProject[].class, listener, errorListener);
		requestQueue.add(req);
	}

	/**
	 * Returns a list of all issues for the given project. The issues must be in
	 * progress.
	 *
	 * @param projectKey
	 *            The project of which the issues are requested.
	 * @param listener
	 *            A listener which will be callef if the request suceeds to
	 *            handle the result.
	 * @param errorListener
	 *            A listener which will be called if an error occurs.
	 */
	public void getAssignedIssuess(String projectKey,
			Listener<JiraIssue[]> listener, ErrorListener errorListener) {
		// Wrap the listener into a custom jql listener to hide the jql response
		// object.
		final GsonRequest<JqlObject> req = createBaseRequest(
				ISSUES_TEMPLATE.replace(REPLACE_CHAR, projectKey),
				JqlObject.class, createJqlIssueListener(listener),
				errorListener);
		requestQueue.add(req);
	}

	private Listener<JqlObject> createJqlIssueListener(
			Listener<JiraIssue[]> listener) {
		return new JqlIssueListener(listener);
	}
}
