package org.sagebionetworks.web.client.widget.entity.renderer;
import static org.sagebionetworks.web.client.SynapseJavascriptClient.ACCEPT;
import static org.sagebionetworks.web.shared.WebConstants.NBCONVERT_ENDPOINT_PROPERTY;
import static org.sagebionetworks.web.shared.WebConstants.TEXT_HTML_CHARSET_UTF8;

import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class NbConvertPreviewWidget extends HtmlPreviewWidget {
	String nbConvertEndpoint;
	GWTWrapper gwt;
	public static final String HTML_PREFIX = "<html><head>" + 
			"<link rel=\"stylesheet\" type=\"text/css\" href=\"css\\notebook.css\">" + 
			"</head><body>";
	public static final String HTML_SUFFIX = "</body></html>";
	@Inject
	public NbConvertPreviewWidget(
			HtmlPreviewView view,
			PresignedURLAsyncHandler presignedURLAsyncHandler,
			SynapseJSNIUtils jsniUtils,
			RequestBuilderWrapper requestBuilder,
			SynapseAlert synAlert,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalAppState,
			PopupUtilsView popupUtils,
			GWTWrapper gwt) {
		super(view, presignedURLAsyncHandler, jsniUtils, requestBuilder, synAlert, synapseClient, popupUtils);
		this.gwt = gwt;
		nbConvertEndpoint = globalAppState.getSynapseProperty(NBCONVERT_ENDPOINT_PROPERTY);
		view.setShowContentLinkText("Download this Juypter notebook and run in a local notebook server to see the fully interactive version.");
	}
	
	@Override
	public void setPresignedUrl(String url) {
		view.setSanitizedWarningVisible(true);
		String encodedUrl = gwt.encodeQueryString(url);
		//use lambda endpoint to resolve ipynb file to html
		requestBuilder.configure(RequestBuilder.GET, nbConvertEndpoint+encodedUrl);
		requestBuilder.setHeader(ACCEPT, TEXT_HTML_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						renderHTML(HTML_PREFIX + response.getText() + HTML_SUFFIX);
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
	
	@Override
	public void onShowFullContent() {
		//in this case (ipynb), to show the full content they need to download the ipynb file and view it locally
		presignedURLAsyncHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
			@Override
			public void onSuccess(FileResult fileResult) {
				view.openInNewWindow(fileResult.getPreSignedURL());
			}
			
			@Override
			public void onFailure(Throwable ex) {
				view.setLoadingVisible(false);
				synAlert.handleException(ex);
			}
		});
	}
}
