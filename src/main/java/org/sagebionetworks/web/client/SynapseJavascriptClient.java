package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class SynapseJavascriptClient {
	RequestBuilderWrapper requestBuilderForGet;
	AuthenticationController authController;
	JSONObjectAdapter jsonObjectAdapter;
	
	private static final String ENTITY_URI_PATH = "/entity";
	private static final String ENTITY_BUNDLE_PATH = "/bundle?mask=";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String ACCEPT = "Accept";
	private static final String SESSION_TOKEN_HEADER = "sessionToken";
	private static final String USER_AGENT = "User-Agent";
	private static final String SYNAPSE_ENCODING_CHARSET = "UTF-8";
	private static final String APPLICATION_JSON_CHARSET_UTF8 = "application/json; charset="+SYNAPSE_ENCODING_CHARSET;
	
	@Inject
	public SynapseJavascriptClient(
			RequestBuilderWrapper requestBuilder,
			AuthenticationController authController,
			JSONObjectAdapter jsonObjectAdapter) {
		this.requestBuilderForGet = requestBuilder;
		this.authController = authController;
		this.jsonObjectAdapter = jsonObjectAdapter;
	}
	
	public void getEntityBundle(String entityId, int partsMask, final AsyncCallback<EntityBundle> callback) {
		requestBuilderForGet.configure(RequestBuilder.GET, "https://repo-prod.prod.sagebase.org/repo/v1" + ENTITY_URI_PATH + "/"+ entityId + ENTITY_BUNDLE_PATH + partsMask);
		requestBuilderForGet.setHeader(ACCEPT, APPLICATION_JSON_CHARSET_UTF8);
		if (authController.isLoggedIn()) {
			// TODO: bug in backend, CORS preflight response must acknowledge custom headers for actual request to work.
			// Currently, "sessionToken" is not sent back in Access-Control-Allow-Headers, so it fails.
			requestBuilderForGet.setHeader(SESSION_TOKEN_HEADER, authController.getCurrentUserSessionToken());
		}
		
		try {
			requestBuilderForGet.sendRequest(null, new RequestCallback() {

				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						String json = response.getText();
						try {
							JSONObjectAdapter jsonObject = jsonObjectAdapter.createNew(json);
							callback.onSuccess(new EntityBundle(jsonObject));
						} catch (JSONObjectAdapterException e) {
							onError(request, e);
						}
					} else {
						onError(request, new IllegalArgumentException("Unable to make call. Reason: " + response.getStatusText()));
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					callback.onFailure(exception);
				}
			});
		} catch (final Exception e) {
			callback.onFailure(e);
		}
	}
}
