package de.htwg.tqm.app.model;

import android.util.Base64;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import de.htwg.tqm.app.volley.GsonRequest;
import de.htwg.tqm.app.volley.JiraApi;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class JiraApiTest {

	private static final String URI_SUFFIX = "rest/api/2/";
	private static final String USER = "user";
	private static final String PASS = "pass";
	private boolean listenerCalled;

	private RequestQueue createQueue() {
        return Volley.newRequestQueue(RuntimeEnvironment.application);
	}

    @Test
	public void testValidConstructorArgs() {
		final String uri = "http://localhost/";
		final JiraApi api = new JiraApi(uri, USER, PASS, createQueue());

        assertThat(uri + URI_SUFFIX, is(api.getUri()));
        assertThat(USER, is(api.getUser()));
        assertThat(PASS, is(api.getPass()));
	}

    @Test
	public void testMissingTrailingSlash() {
		final String uri = "http://localhost";
		final JiraApi api = new JiraApi(uri, USER, PASS, createQueue());

        assertThat(uri + "/" + URI_SUFFIX, is(api.getUri()));
	}

    @Test
	public void testMissingLeadingHttp() {
		final String uri = "localhost/";
		final JiraApi api = new JiraApi(uri, USER, PASS, createQueue());

        assertThat("http://" + uri + URI_SUFFIX, is(api.getUri()));
	}

	private void setListenerCalled(boolean value) {
		listenerCalled = value;
	}

    @Test
	public void testGetRequestWithCredentials() throws Exception {
		final String uri = "http://localhost/";
		final String resource = "res";
		final JiraApi api = new JiraApi(uri, USER, PASS, createQueue());
		final GsonRequest<String> request = api.createBaseRequest(resource,
				String.class, new Listener<String>() {
					@Override
					public void onResponse(String res) {
					}
				}, createFailFastErrorListener());

		final String userPass = (USER + ":" + PASS);
		final String expectedCredentials = "Basic "
				+ Base64.encodeToString(userPass.getBytes("UTF-8"),
						Base64.NO_WRAP);

        assertThat(expectedCredentials,
                is(request.getHeaders().get("Authorization")));
        assertThat(uri + URI_SUFFIX + resource, is(request.getUrl()));
	}

    @Test
	public void testGetRequestListener() {
		final String uri = "http://localhost/";
		final String expectedResponse = "resp";
		final JiraApi api = new JiraApi(uri, USER, PASS, createQueue());
		final GsonRequest<String> request = api.createBaseRequest("/res",
				String.class, new Listener<String>() {
					@Override
					public void onResponse(String res) {
						assertEquals(expectedResponse, res);
						setListenerCalled(true);
					}
				}, createFailFastErrorListener());

		setListenerCalled(false);
		request.deliverResponse(expectedResponse);
		assertTrue(listenerCalled);
	}

	private ErrorListener createFailFastErrorListener() {
		return new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				fail("an error ocurred");
			}
		};
	}

}
