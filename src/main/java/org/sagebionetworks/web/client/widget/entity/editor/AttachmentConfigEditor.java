package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.WikiAttachments;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentConfigEditor implements AttachmentConfigView.Presenter, WidgetEditorPresenter {

	private AttachmentConfigView view;
	private Map<String, String> descriptor;
	private List<String> fileHandleIds;
	private FileHandleUploadWidget fileInputWidget;
	private FileUpload uploadedFile;
	private WikiAttachments wikiAttachments;


	@Inject
	public AttachmentConfigEditor(AttachmentConfigView view, FileHandleUploadWidget fileInputWidget, WikiAttachments wikiAttachments) {
		this.view = view;
		this.fileInputWidget = fileInputWidget;
		this.wikiAttachments = wikiAttachments;
		view.setPresenter(this);
		view.setFileInputWidget(fileInputWidget.asWidget());
		view.setWikiAttachmentsWidget(wikiAttachments.asWidget());
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, final DialogCallback dialogCallback) {
		view.initView();
		descriptor = widgetDescriptor;
		fileHandleIds = new ArrayList<String>();
		fileInputWidget.reset();
		view.clear();
		view.configure(wikiKey, dialogCallback);
		wikiAttachments.configure(wikiKey);
		uploadedFile = null;
		view.setWikiAttachmentsWidgetVisible(true);
		this.fileInputWidget.configure(WebConstants.DEFAULT_FILE_HANDLE_WIDGET_TEXT, new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload uploadFile) {
				view.setWikiAttachmentsWidgetVisible(false);
				// enable the ok button
				uploadedFile = uploadFile;
				view.showUploadSuccessUI(getFileName());
				dialogCallback.setPrimaryEnabled(true);
				addFileHandleId(uploadFile.getFileHandleId());
			}
		});
	}


	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		view.checkParams();
		if (!fileHandleIds.isEmpty()) {
			descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, getFileName());
		} else {
			if (!wikiAttachments.isValid())
				throw new IllegalArgumentException(DisplayConstants.ERROR_SELECT_ATTACHMENT_MESSAGE);
			descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, wikiAttachments.getSelectedFilename());
		}
	}

	private String getFileName() {
		if (uploadedFile == null)
			return null;
		else
			return uploadedFile.getFileMeta().getFileName();

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
		if (fileHandleIds.isEmpty()) {
			return wikiAttachments.getFilesHandlesToDelete();
		} else {
			return null;
		}
	}
	/*
	 * Private Methods
	 */
}
