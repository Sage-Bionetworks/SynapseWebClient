package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.SynapseJavascriptClient.ACCEPT;
import static org.sagebionetworks.web.shared.WebConstants.NBCONVERT_ENDPOINT_PROPERTY;
import static org.sagebionetworks.web.shared.WebConstants.TEXT_HTML_CHARSET_UTF8;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class NbConvertPreviewWidget extends HtmlPreviewWidget {
	public static final String DOWNLOAD_NOTEBOOK_MESSAGE = "Download this Juypter notebook and run in a local notebook server to see the fully interactive version.";
	String nbConvertEndpoint;
	public static final String HTML_PREFIX = "<html><head>" + "<link rel=\"stylesheet\" type=\"text/css\" href=\"css\\notebook.css\">" + "</head><body>";
	public static final String HTML_SUFFIX = "</body></html>";

	@Inject
	public NbConvertPreviewWidget(HtmlPreviewView view, PresignedURLAsyncHandler presignedURLAsyncHandler, SynapseJSNIUtils jsniUtils, RequestBuilderWrapper requestBuilder, SynapseAlert synAlert, SynapseClientAsync synapseClient, PopupUtilsView popupUtils, SynapseProperties synapseProperties, GWTWrapper gwt) {
		super(view, presignedURLAsyncHandler, jsniUtils, requestBuilder, synAlert, synapseClient, popupUtils, gwt);
		nbConvertEndpoint = synapseProperties.getSynapseProperty(NBCONVERT_ENDPOINT_PROPERTY);
		view.setShowContentLinkText(DOWNLOAD_NOTEBOOK_MESSAGE);
	}

	@Override
	public void renderHTML(String rawHtml) {
		super.renderHTML(HTML_PREFIX + rawHtml + HTML_SUFFIX);
	}

	@Override
	public void setPresignedUrl(String url) {
		view.setSanitizedWarningVisible(true);
		String encodedUrl = gwt.encodeQueryString(url);
		// use lambda endpoint to resolve ipynb file to html
		requestBuilder.configure(RequestBuilder.GET, nbConvertEndpoint + encodedUrl);
		requestBuilder.setHeader(ACCEPT, TEXT_HTML_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(null, getRequestCallback());
		} catch (final Exception e) {
			view.setLoadingVisible(false);
			synAlert.handleException(e);
		}
	}

	@Override
	public void onShowFullContent() {
		// in this case (ipynb), to show the full content they need to download the ipynb file and view it
		// locally
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
