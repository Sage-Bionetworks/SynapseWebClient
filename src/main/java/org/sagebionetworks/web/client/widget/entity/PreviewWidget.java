package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PreviewWidget implements PreviewWidgetView.Presenter{
	
	PreviewWidgetView view;
	RequestBuilderWrapper requestBuilder;
	SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public PreviewWidget(PreviewWidgetView view, RequestBuilderWrapper requestBuilder,SynapseJSNIUtils synapseJSNIUtils) {
		this.view = view;
		this.requestBuilder = requestBuilder;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	public Widget asWidget(EntityBundle bundle) {
		view.clear();
		PreviewFileHandle handle = FileTitleBar.getPreviewFileHandle(bundle);
		FileHandle originalFileHandle = FileTitleBar.getFileHandle(bundle);
		if (handle != null) {
			final String contentType = handle.getContentType();
			final String fileName = handle.getFileName();
			if (contentType != null) {
				FileEntity fileEntity = (FileEntity)bundle.getEntity();
				if (DisplayUtils.isRecognizedImageContentType(contentType)) {
					//add a html panel that contains the image src from the attachments server (to pull asynchronously)
					//create img
					view.setImagePreview(DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(), ((Versionable)fileEntity).getVersionNumber(), false), 
										DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(),  ((Versionable)fileEntity).getVersionNumber(), true));
				}
				else {
					final boolean isCode = ContentTypeUtils.isRecognizedCodeFileName(originalFileHandle.getFileName());
					final boolean isTextType = DisplayUtils.isTextType(contentType);
					if (isTextType) {
						final boolean isCSV = DisplayUtils.isCSV(contentType);
						//try to load the text of the preview, if available
						//must have file handle servlet proxy the request to the endpoint (because of cross-domain access restrictions)
						requestBuilder.configure(RequestBuilder.GET,DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(),  ((Versionable)fileEntity).getVersionNumber(), true, true));
						
						try {
							requestBuilder.sendRequest(null, new RequestCallback() {
								public void onError(final Request request, final Throwable e) {
									view.showErrorMessage(e.getMessage());
								}
								public void onResponseReceived(final Request request, final Response response) {
									//add the response text
									int statusCode = response.getStatusCode();
									if (statusCode == Response.SC_OK) {
										String responseText = response.getText();
										if (responseText != null && responseText.length() > 0) {
											if (isCode) {
												view.setCodePreview(SafeHtmlUtils.htmlEscapeAllowEntities(responseText));
											} else if (isCSV){
												view.setTablePreview(SafeHtmlUtils.htmlEscapeAllowEntities(responseText));
											} else if (isTextType){
												view.setBlockQuotePreview(SafeHtmlUtils.htmlEscapeAllowEntities(responseText));
											}
										}
									}
								}
							});
						} catch (final Exception e) {
							view.showErrorMessage(e.getMessage());
						}
					}
				}
			}
		}
		return view.asWidget();
	}
}
