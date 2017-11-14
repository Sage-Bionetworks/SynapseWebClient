package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Widget used to show untrusted html preview content.  Has an option to open the untrusted html in a new window (after confirmation)
 * @author jayhodgson
 *
 */
public class HtmlPreviewWidget implements IsWidget {
	protected HtmlPreviewView view;
	protected PresignedURLAsyncHandler presignedURLAsyncHandler;
	protected FileHandleAssociation fha;
	protected SynapseAlert synAlert;
	protected RequestBuilderWrapper requestBuilder;
	protected SynapseJSNIUtils jsniUtils;
	protected String rawHtml;
	
	@Inject
	public HtmlPreviewWidget(
			HtmlPreviewView view,
			PresignedURLAsyncHandler presignedURLAsyncHandler,
			SynapseJSNIUtils jsniUtils,
			RequestBuilderWrapper requestBuilder,
			SynapseAlert synAlert) {
		this.view = view;
		this.presignedURLAsyncHandler = presignedURLAsyncHandler;
		this.jsniUtils = jsniUtils;
		this.requestBuilder = requestBuilder;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
	}
	
	public void configure(String synapseId, String fileHandleId) {
		fha = new FileHandleAssociation();
		fha.setAssociateObjectId(synapseId);
		fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
		fha.setFileHandleId(fileHandleId);
		refreshContent();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void refreshContent() {
		if (fha != null) {
			synAlert.clear();
			view.setLoadingVisible(true);
			presignedURLAsyncHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
				@Override
				public void onSuccess(FileResult fileResult) {
					setPresignedUrl(fileResult.getPreSignedURL());
				}
				
				@Override
				public void onFailure(Throwable ex) {
					view.setLoadingVisible(false);
					synAlert.handleException(ex);
				}
			});
		}
	}
	
	public void setPresignedUrl(String url) {
		// by default, get url.
		// TODO: write a new class, NbconvertHtmlPreviewWidget that uses the lambda web service to convert the ipynb to html.
		requestBuilder.configure(RequestBuilder.GET, url.toString());
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
	
	public void setHtml(String html) {
		this.rawHtml = html;
		//sanitize before rendering
		String sanitizedHtml = jsniUtils.sanitizeHtml(html);
		view.setHtml(sanitizedHtml);
	}
}
