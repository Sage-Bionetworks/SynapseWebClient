package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.WikiAttachments;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigEditor implements ImageConfigView.Presenter, WidgetEditorPresenter {
	
	private ImageConfigView view;
	private Map<String, String> descriptor;
	private List<String> fileHandleIds;
	private FileInputWidget fileInputWidget;
	private DialogCallback dialogCallback;
	private WikiAttachments wikiAttachments;
	
	@Inject
	public ImageConfigEditor(ImageConfigView view, FileInputWidget fileInputWidget, WikiAttachments wikiAttachments) {
		this.view = view;
		view.setPresenter(this);
		this.fileInputWidget = fileInputWidget;
		this.wikiAttachments = wikiAttachments;
		view.setFileInputWidget(fileInputWidget.asWidget());
		view.setWikiAttachmentsWidget(wikiAttachments.asWidget());
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		this.dialogCallback = dialogCallback;
		fileHandleIds = new ArrayList<String>();
		view.initView();
		fileInputWidget.reset();
		view.configure(wikiKey, dialogCallback);
		wikiAttachments.configure(wikiKey);
		//and try to prepopulate with values from the map.  if it fails, ignore
		try {
			if (descriptor.containsKey(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY) || descriptor.containsKey(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY)) {
				if (descriptor.containsKey(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY)){
					view.setSynapseId(descriptor.get(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY));
				} 
				view.setAlignment(descriptor.get(WidgetConstants.IMAGE_WIDGET_ALIGNMENT_KEY));
			}
		} catch (Exception e) {}
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		view.checkParams();
		if (!view.isExternal()) {
			if (view.isSynapseEntity()) {
				descriptor.put(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY, view.getSynapseId());
			} else if (view.isFromAttachments()) {
				if (!wikiAttachments.isValid()) {
					throw new IllegalArgumentException(DisplayConstants.ERROR_SELECT_ATTACHMENT_MESSAGE);
				}
				descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, wikiAttachments.getSelectedFilename());
			} else {
				if (fileHandleIds.isEmpty()) {
					throw new IllegalArgumentException(DisplayConstants.IMAGE_CONFIG_UPLOAD_FIRST_MESSAGE);
				}
				descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, getFileName());	
			}
				
			descriptor.put(WidgetConstants.IMAGE_WIDGET_ALIGNMENT_KEY, view.getAlignment());
		}
	}
	
	private String getFileName() {
		if (validateSelectedFile())
			return fileInputWidget.getSelectedFileMetadata()[0].getFileName();
		else return null;
	}
	
	public boolean validateSelectedFile() {
		FileMetadata[] meta = fileInputWidget.getSelectedFileMetadata();
		if(meta == null || meta.length != 1){
			view.showErrorMessage("Please select a file and try again");
			return false;
		} else {
			String fileName = fileInputWidget.getSelectedFileMetadata()[0].getFileName();
			String extension = fileName.substring(fileName.lastIndexOf(".")+1);
			 if (!DisplayUtils.isRecognizedImageContentType("image/"+extension)) {
				 view.showErrorMessage(DisplayConstants.IMAGE_CONFIG_FILE_TYPE_MESSAGE);
				 return false;
			 }
		}
		return true;
	}
	
	@Override
	public void uploadFileClicked() {
		if (validateSelectedFile()) {
			view.setUploadButtonEnabled(false);
			fileInputWidget.uploadSelectedFile(new FileUploadHandler() {
				@Override
				public void uploadSuccess(String fileHandleId) {
					view.showUploadSuccessUI();
					//enable the ok button
					dialogCallback.setPrimaryEnabled(true);
					addFileHandleId(fileHandleId);
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
		if (view.isExternal())
			return "!["+view.getAltText()+"]("+view.getImageUrl()+")";
		else return null;
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
