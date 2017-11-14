package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class NbConvertPreviewWidget extends HtmlPreviewWidget {

	@Inject
	public NbConvertPreviewWidget(
			HtmlPreviewView view,
			PresignedURLAsyncHandler presignedURLAsyncHandler,
			SynapseJSNIUtils jsniUtils,
			RequestBuilderWrapper requestBuilder,
			SynapseAlert synAlert) {
		super(view, presignedURLAsyncHandler, jsniUtils, requestBuilder, synAlert);
	}
	
	@Override
	public void setPresignedUrl(String url) {
		// use the lambda web service to convert the ipynb file to html.
		//TODO: set up request
		requestBuilder.configure(RequestBuilder.POST, url.toString());
		requestBuilder.setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_HTML_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {

				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						String html = response.getText();
						view.setLoadingVisible(false);
						setHtml(html);
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
