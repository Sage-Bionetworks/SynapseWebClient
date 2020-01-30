package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ClientProperties.MB;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.GWTWrapper;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Widget used to show untrusted html preview content. Has an option to open the untrusted html in a
 * new window (after confirmation)
 * 
 * @author jayhodgson
 *
 */
public class HtmlPreviewWidget implements IsWidget, HtmlPreviewView.Presenter {
	public static final String CONFIRM_OPEN_HTML_MESSAGE = "Click \"OK\" to leave this page and open this content in a new window; this enables additional functionality, but should only be done if you trust the content.";
	protected HtmlPreviewView view;
	protected PresignedURLAsyncHandler presignedURLAsyncHandler;
	protected FileHandleAssociation fha;
	protected SynapseAlert synAlert;
	protected RequestBuilderWrapper requestBuilder;
	protected SynapseJSNIUtils jsniUtils;
	protected String createdBy;
	protected SynapseClientAsync synapseClient;
	protected PopupUtilsView popupUtils;
	protected GWTWrapper gwt;

	public static final double MAX_HTML_FILE_SIZE = 40 * MB;
	public static String friendlyMaxFileSize = null;

	@Inject
	public HtmlPreviewWidget(HtmlPreviewView view, PresignedURLAsyncHandler presignedURLAsyncHandler, SynapseJSNIUtils jsniUtils, RequestBuilderWrapper requestBuilder, SynapseAlert synAlert, SynapseClientAsync synapseClient, PopupUtilsView popupUtils, GWTWrapper gwt) {
		this.view = view;
		this.presignedURLAsyncHandler = presignedURLAsyncHandler;
		this.jsniUtils = jsniUtils;
		this.requestBuilder = requestBuilder;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.popupUtils = popupUtils;
		this.gwt = gwt;
		view.setSynAlert(synAlert);
		view.setPresenter(this);
		if (friendlyMaxFileSize == null) {
			friendlyMaxFileSize = gwt.getFriendlySize(MAX_HTML_FILE_SIZE, true);
		}
	}

	public void renderHTML(final String rawHtml) {
		synapseClient.isUserAllowedToRenderHTML(createdBy, new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setLoadingVisible(false);
				showSanitizedHtml();
				jsniUtils.consoleError(caught.getMessage());
			}

			@Override
			public void onSuccess(Boolean trustedUser) {
				view.setLoadingVisible(false);
				if (trustedUser) {
					view.setHtml(rawHtml);
				} else {
					showSanitizedHtml();
				}
			}

			private void showSanitizedHtml() {
				// is the sanitized version the same as the original??
				String sanitizedHtml = jsniUtils.sanitizeHtml(rawHtml);
				if (rawHtml.equals(sanitizedHtml)) {
					view.setHtml(rawHtml);
				} else {
					view.setHtml(sanitizedHtml);
					view.setRawHtml(rawHtml);
					view.setSanitizedWarningVisible(true);
				}
			}
		});
	}

	public void configure(String synapseId, FileHandle fileHandle) {
		this.createdBy = fileHandle.getCreatedBy();
		fha = new FileHandleAssociation();
		fha.setAssociateObjectId(synapseId);
		fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
		fha.setFileHandleId(fileHandle.getId());
		if (fileHandle.getContentSize() != null && fileHandle.getContentSize() < MAX_HTML_FILE_SIZE) {
			refreshContent();
		} else {
			view.setLoadingVisible(false);
			synAlert.showError("The preview was not shown because the size (" + gwt.getFriendlySize(fileHandle.getContentSize().doubleValue(), true) + ") exceeds the maximum preview size (" + friendlyMaxFileSize + ")");
		}
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
			requestBuilder.sendRequest(null, getRequestCallback());
		} catch (final Exception e) {
			view.setLoadingVisible(false);
			synAlert.handleException(e);
		}
	}

	protected RequestCallback getRequestCallback() {
		return new RequestCallback() {
			@Override
			public void onResponseReceived(Request request, Response response) {
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
		};
	}

	@Override
	public void onShowFullContent() {
		// confirm
		popupUtils.showConfirmDialog("", CONFIRM_OPEN_HTML_MESSAGE, () -> {
			// user clicked yes
			view.openRawHtmlInNewWindow();
		});
	}
}
