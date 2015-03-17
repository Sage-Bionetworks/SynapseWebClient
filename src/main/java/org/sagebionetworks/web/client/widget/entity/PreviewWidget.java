package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.repo.model.EntityBundle;

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
	
	@Inject
	public PreviewWidget(PreviewWidgetView view, RequestBuilderWrapper requestBuilder,SynapseJSNIUtils synapseJSNIUtils) {
		this.view = view;
		this.requestBuilder = requestBuilder;
		this.synapseJSNIUtils = synapseJSNIUtils;
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
		if (bundle != null) {
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
								view.showErrorMessage(DisplayConstants.PREVIEW_FAILED_TEXT + SafeHtmlUtils.htmlEscapeAllowEntities(e.getMessage()));
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
						view.showErrorMessage(DisplayConstants.PREVIEW_FAILED_TEXT+SafeHtmlUtils.htmlEscapeAllowEntities(e.getMessage()));
					}
				}
			}
		}
		return view.asWidget();
	}
}
