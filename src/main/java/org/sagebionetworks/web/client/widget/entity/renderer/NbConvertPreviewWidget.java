package org.sagebionetworks.web.client.widget.entity.renderer;
import static org.sagebionetworks.web.client.SynapseJavascriptClient.*;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import static org.sagebionetworks.web.shared.WebConstants.*;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class NbConvertPreviewWidget extends HtmlPreviewWidget {

	JSONObjectAdapter jsonObjectAdapter;
	String nbConvertEndpoint;
	@Inject
	public NbConvertPreviewWidget(
			HtmlPreviewView view,
			PresignedURLAsyncHandler presignedURLAsyncHandler,
			SynapseJSNIUtils jsniUtils,
			RequestBuilderWrapper requestBuilder,
			SynapseAlert synAlert,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalAppState) {
		super(view, presignedURLAsyncHandler, jsniUtils, requestBuilder, synAlert, synapseClient);
		this.jsonObjectAdapter = jsonObjectAdapter;
		nbConvertEndpoint = globalAppState.getSynapseProperty(NBCONVERT_ENDPOINT_PROPERTY);
	}
	
	@Override
	public void setPresignedUrl(String url) {
		// use the lambda web service to convert the ipynb file to html.
		//TODO: set up request
		
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			adapter.put("file", url);
		} catch (JSONObjectAdapterException exception) {
			return;
		}
		//TODO: use lambda endpoint to resolve ipynb file to html
		requestBuilder.configure(RequestBuilder.POST, nbConvertEndpoint);
		requestBuilder.setHeader(ACCEPT, TEXT_HTML_CHARSET_UTF8);
		requestBuilder.setHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(adapter.toJSONString(), new RequestCallback() {
				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						rawHtml = response.getText();
						view.setLoadingVisible(false);
						renderHTML();
					} else {
						onError(null, new IllegalArgumentException("Unable to retrieve. Reason: " + response.getStatusText()));
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					view.setLoadingVisible(false);
					synAlert.handleException(exception);
				}
			});
		} catch (final Exception e) {
			view.setLoadingVisible(false);
			synAlert.handleException(e);
		}
	}
}
