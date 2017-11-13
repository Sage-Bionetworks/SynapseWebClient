package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.utils.CajaHtmlSanitizer;
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

public class NbviewerPreviewWidget implements IsWidget {
	public static final String NBVIEWER = "http://nbviewer.jupyter.org/urls/";
	private HtmlView view;
	private PresignedURLAsyncHandler presignedURLAsyncHandler;
	private FileHandleAssociation fha;
	private CajaHtmlSanitizer cajaHtmlSanitizer;
	private SynapseAlert synAlert;
	private RequestBuilderWrapper requestBuilder;
	@Inject
	public NbviewerPreviewWidget(
			HtmlView view,
			PresignedURLAsyncHandler presignedURLAsyncHandler,
			CajaHtmlSanitizer cajaHtmlSanitizer,
			RequestBuilderWrapper requestBuilder,
			SynapseAlert synAlert) {
		this.view = view;
		this.presignedURLAsyncHandler = presignedURLAsyncHandler;
		this.cajaHtmlSanitizer = cajaHtmlSanitizer;
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
		StringBuilder siteUrl = new StringBuilder();
		siteUrl.append(NBVIEWER);
		siteUrl.append(url.substring(url.indexOf("://")));

		// get html nbviewer html response
		requestBuilder.configure(RequestBuilder.GET, siteUrl.toString());
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
		//sanitize before rendering
		String sanitizedHtml = cajaHtmlSanitizer.sanitize(html);
		view.setHtml(sanitizedHtml);
	}
}
