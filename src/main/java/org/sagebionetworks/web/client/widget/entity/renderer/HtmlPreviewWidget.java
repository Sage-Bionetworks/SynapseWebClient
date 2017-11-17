package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Widget used to show untrusted html preview content.  Has an option to open the untrusted html in a new window (after confirmation)
 * @author jayhodgson
 *
 */
public class HtmlPreviewWidget implements IsWidget, HtmlPreviewView.Presenter {
	protected HtmlPreviewView view;
	protected PresignedURLAsyncHandler presignedURLAsyncHandler;
	protected FileHandleAssociation fha;
	protected SynapseAlert synAlert;
	protected RequestBuilderWrapper requestBuilder;
	protected SynapseJSNIUtils jsniUtils;
	protected String createdBy;
	protected SynapseClientAsync synapseClient;
	protected PopupUtilsView popupUtils;
	@Inject
	public HtmlPreviewWidget(
			HtmlPreviewView view,
			PresignedURLAsyncHandler presignedURLAsyncHandler,
			SynapseJSNIUtils jsniUtils,
			RequestBuilderWrapper requestBuilder,
			SynapseAlert synAlert,
			SynapseClientAsync synapseClient,
			PopupUtilsView popupUtils) {
		this.view = view;
		this.presignedURLAsyncHandler = presignedURLAsyncHandler;
		this.jsniUtils = jsniUtils;
		this.requestBuilder = requestBuilder;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
		this.popupUtils = popupUtils;
		view.setSynAlert(synAlert);
		view.setPresenter(this);
	}
	
	public void renderHTML(final String rawHtml) {
		view.setRawHtml(rawHtml);
		synapseClient.isUserAllowedToRenderHTML(createdBy, new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setLoadingVisible(false);
				String escapedContent = SafeHtmlUtils.htmlEscapeAllowEntities(rawHtml);
				view.setHtml(truncateLargeHtml(escapedContent));
				view.setSanitizedWarningVisible(true);
			}
			
			@Override
			public void onSuccess(Boolean trustedUser) {
				view.setLoadingVisible(false);
				if (trustedUser) {
					view.setHtml(rawHtml);
				} else {
					// is the sanitized version the same as the original??
					String sanitizedHtml = jsniUtils.sanitizeHtml(rawHtml);
					if (rawHtml.equals(sanitizedHtml)) {
						view.setHtml(rawHtml);
					} else {
						view.setHtml(truncateLargeHtml(sanitizedHtml));
						view.setSanitizedWarningVisible(true);
					}
				}
			}
			
			public String truncateLargeHtml(String sanitizedHtml) {
				if (sanitizedHtml.length() > 20000) {
					return sanitizedHtml.substring(0, 20000) + "\n...";
				} else {
					return sanitizedHtml;
				}
			}
		});
	}
	
	public void configure(String synapseId, String fileHandleId, String fileHandleCreatedBy) {
		this.createdBy = fileHandleCreatedBy;
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
			view.setSanitizedWarningVisible(false);
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
		requestBuilder.configure(RequestBuilder.GET, url.toString());
		requestBuilder.setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_HTML_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						renderHTML(response.getText());
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
		//confirm
		popupUtils.showConfirmDialog("", "Click \"OK\" to leave this page and open this content in a new window; this enables additional functionality, but should only be done if you trust the content.", () -> {
			//user clicked yes
			view.openRawHtmlInNewWindow();
		});
	}
}
