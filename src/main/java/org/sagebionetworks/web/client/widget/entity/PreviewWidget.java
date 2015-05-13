package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PreviewWidget implements PreviewWidgetView.Presenter{
	public static final String APPLICATION_ZIP = "application/zip";	
	
	public enum PreviewFileType {
		PLAINTEXT, CODE, ZIP, CSV, IMAGE, NONE, TAB
	}

	
	PreviewWidgetView view;
	RequestBuilderWrapper requestBuilder;
	SynapseJSNIUtils synapseJSNIUtils;
	EntityBundle bundle;
	SynapseAlert synapseAlert;
	
	@Inject
	public PreviewWidget(PreviewWidgetView view, 
			RequestBuilderWrapper requestBuilder,
			SynapseJSNIUtils synapseJSNIUtils,
			SynapseAlert synapseAlert) {
		this.view = view;
		this.requestBuilder = requestBuilder;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.synapseAlert = synapseAlert;
	}
	
	public PreviewFileType getPreviewFileType(PreviewFileHandle previewHandle, FileHandle originalFileHandle) {
		PreviewFileType previewFileType = PreviewFileType.NONE;
		if (previewHandle != null && originalFileHandle != null) {
			String contentType = previewHandle.getContentType();
			if (contentType != null) {
				if (DisplayUtils.isRecognizedImageContentType(contentType)) {
					previewFileType = PreviewFileType.IMAGE;
				}
				else if (DisplayUtils.isTextType(contentType)) {
					//some kind of text
					if (ContentTypeUtils.isRecognizedCodeFileName(originalFileHandle.getFileName())){
						previewFileType = PreviewFileType.CODE;
					}
					else if (DisplayUtils.isCSV(contentType)) {
						if (APPLICATION_ZIP.equals(originalFileHandle.getContentType()))
							previewFileType = PreviewFileType.ZIP;
						else
							previewFileType = PreviewFileType.CSV;
					}
					else if (DisplayUtils.isCSV(originalFileHandle.getContentType())){
						previewFileType = PreviewFileType.CSV;
					}
					else if (DisplayUtils.isTAB(contentType) || DisplayUtils.isTAB(originalFileHandle.getContentType())) {
						previewFileType = PreviewFileType.TAB;
					}
					else {
						previewFileType = PreviewFileType.PLAINTEXT;
					}
				}
			}
		}
		return previewFileType;
	}
	public void configure(EntityBundle bundle) {
		this.bundle = bundle;
	}
	
	public Widget asWidget() {
		view.clear();
		
		//if not logged in, don't even try to load the preview.  Just direct user to log in.
		if (!synapseAlert.isUserLoggedIn()) {
			view.addSynapseAlertWidget(synapseAlert.asWidget());
			synapseAlert.showMustLogin();
		} else if (bundle != null) {
			PreviewFileHandle handle = DisplayUtils.getPreviewFileHandle(bundle);
			FileHandle originalFileHandle = DisplayUtils.getFileHandle(bundle);
			final PreviewFileType previewType = getPreviewFileType(handle, originalFileHandle);
			if (previewType != PreviewFileType.NONE) {
				FileEntity fileEntity = (FileEntity)bundle.getEntity();
				if (previewType == PreviewFileType.IMAGE) {
					//add a html panel that contains the image src from the attachments server (to pull asynchronously)
					//create img
					view.setImagePreview(DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(), ((Versionable)fileEntity).getVersionNumber(), false), 
										DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(),  ((Versionable)fileEntity).getVersionNumber(), true));
				}
				else { //must be a text type of some kind
					//try to load the text of the preview, if available
					//must have file handle servlet proxy the request to the endpoint (because of cross-domain access restrictions)
					requestBuilder.configure(RequestBuilder.GET,DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(),  ((Versionable)fileEntity).getVersionNumber(), true, true));
					try {
						requestBuilder.sendRequest(null, new RequestCallback() {
							public void onError(final Request request, final Throwable e) {
								view.addSynapseAlertWidget(synapseAlert.asWidget());
								synapseAlert.handleException(e);
							}
							public void onResponseReceived(final Request request, final Response response) {
								//add the response text
							int statusCode = response.getStatusCode();
								if (statusCode == Response.SC_OK) {
									String responseText = response.getText();
									if (responseText != null && responseText.length() > 0) {
										if (PreviewFileType.CODE == previewType) {
											view.setCodePreview(SafeHtmlUtils.htmlEscapeAllowEntities(responseText));
										} 
										else if (PreviewFileType.CSV == previewType)
											view.setTablePreview(responseText, ",");
										else if (PreviewFileType.TAB == previewType)
											view.setTablePreview(responseText, "\\t");
										else if (PreviewFileType.PLAINTEXT == previewType || PreviewFileType.ZIP == previewType){
											view.setTextPreview(SafeHtmlUtils.htmlEscapeAllowEntities(responseText));
										}
									}
								}
							}
						});
					} catch (final Exception e) {
						view.addSynapseAlertWidget(synapseAlert.asWidget());
						synapseAlert.handleException(e);
					}
				}
			}
		}
		
		return view.asWidget();
	}
	
	@Override
	public void imagePreviewLoadFailed(ErrorEvent e) {
		//show the load error
		view.addSynapseAlertWidget(synapseAlert.asWidget());
		synapseAlert.showError("Unable to load image preview");
	}
}
