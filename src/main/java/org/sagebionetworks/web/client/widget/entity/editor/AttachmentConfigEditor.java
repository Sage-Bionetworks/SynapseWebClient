package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.WikiAttachments;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.UploadedFile;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentConfigEditor implements AttachmentConfigView.Presenter, WidgetEditorPresenter {
	
	private AttachmentConfigView view;
	private Map<String, String> descriptor;
	private List<String> fileHandleIds;
	private FileInputWidget fileInputWidget;
	private DialogCallback dialogCallback;
	private WikiAttachments wikiAttachments;
	
	@Inject
	public AttachmentConfigEditor(AttachmentConfigView view, FileInputWidget fileInputWidget, WikiAttachments wikiAttachments) {
		this.view = view;
		this.fileInputWidget = fileInputWidget;
		this.wikiAttachments = wikiAttachments;
		view.setPresenter(this);
		view.setFileInputWidget(fileInputWidget.asWidget());
		view.setWikiAttachmentsWidget(wikiAttachments.asWidget());
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		view.initView();
		descriptor = widgetDescriptor;
		this.dialogCallback = dialogCallback;
		fileHandleIds = new ArrayList<String>();
		fileInputWidget.reset();
		view.clear();
		view.configure(wikiKey, dialogCallback);
		wikiAttachments.configure(wikiKey);
	}
	
	public boolean validateSelectedFile() {
		FileMetadata[] meta = fileInputWidget.getSelectedFileMetadata();
		if(meta == null || meta.length != 1){
			view.showErrorMessage("Please select a file and try again");
			return false;
		}
		return true;
	}
	
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		view.checkParams();
		if (view.isNewAttachment()) {
			if (fileHandleIds.isEmpty()) {
				throw new IllegalArgumentException(DisplayConstants.IMAGE_CONFIG_UPLOAD_FIRST_MESSAGE);
			}
			descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, getFileName());
		} else if (view.isFromAttachments()) {
			if (!wikiAttachments.isValid())
				throw new IllegalArgumentException(DisplayConstants.ERROR_SELECT_ATTACHMENT_MESSAGE);
			descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, wikiAttachments.getSelectedFilename());
		}
	}
	
	private String getFileName() {
		if (validateSelectedFile())
			return fileInputWidget.getSelectedFileMetadata()[0].getFileName();
		else return null;
	}
	
	@Override
	public void uploadFileClicked() {
		if (validateSelectedFile()) {
			view.setUploadButtonEnabled(false);
			fileInputWidget.uploadSelectedFile(new FileUploadHandler() {
				@Override
				public void uploadSuccess(UploadedFile fileUploaded) {
					view.showUploadSuccessUI();
					//enable the ok button
					dialogCallback.setPrimaryEnabled(true);
					addFileHandleId(fileUploaded.getFileHandleId());
				}
				
				@Override
				public void uploadFailed(String error) {
					view.setUploadButtonEnabled(true);
					view.showUploadFailureUI(error);
				}
			});
		}
	}
	
	@Override
	public String getTextToInsert() {
		return null;
	}
	
	@Override
	public void addFileHandleId(String fileHandleId) {
		fileHandleIds.add(fileHandleId);
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return fileHandleIds;
	}
	
	@Override
	public List<String> getDeletedFileHandleIds() {
		if (view.isFromAttachments()) {
			return wikiAttachments.getFilesHandlesToDelete();
		} else {
			return null;
		}
	}
	/*
	 * Private Methods
	 */
}
