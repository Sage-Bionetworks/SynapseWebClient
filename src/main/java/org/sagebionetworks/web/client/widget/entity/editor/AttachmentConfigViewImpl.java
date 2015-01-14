package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentConfigViewImpl extends FlowPanel implements AttachmentConfigView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private FlowPanel errorPanel = new FlowPanel();
	private FlowPanel uploadNotePanel = new FlowPanel();
	private FileInputWidget fileInputWidget;
	private String uploadedFilename;
	
	@Inject
	public AttachmentConfigViewImpl(IconsImageBundle iconsImageBundle, FileInputWidget fileInputWidget) {
		this.iconsImageBundle = iconsImageBundle;
		this.fileInputWidget = fileInputWidget;
	}
	
	@Override
	public void initView() {
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, DialogCallback dialogCallback) {
		//update the uploadPanel
		initUploadPanel(wikiKey, dialogCallback);
	}
	
	private void initUploadPanel(WikiPageKey wikiKey, final DialogCallback dialogCallback) {
		uploadedFilename = null;
		clear();
		add(uploadNotePanel);
		//The ok/submitting button will be enabled when attachments are uploaded
		dialogCallback.setPrimaryEnabled(false);
		fileInputWidget.reset();
		
		final Button uploadButton = new Button(DisplayConstants.IMAGE_CONFIG_UPLOAD);
		uploadButton.setType(ButtonType.INFO);
		
		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (validateSelectedFile()) {
					uploadButton.setEnabled(false);
					fileInputWidget.uploadSelectedFile(new FileUploadHandler() {
						@Override
						public void uploadSuccess(String fileHandleId) {
							uploadedFilename = fileInputWidget.getSelectedFileMetadata()[0].getFileName();
							clear();
							add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.checkGreen16()) +" "+ DisplayConstants.UPLOAD_SUCCESSFUL_STATUS_TEXT)));
							//enable the ok button
							dialogCallback.setPrimaryEnabled(true);
							presenter.addFileHandleId(fileHandleId);
						}
						
						@Override
						public void uploadFailed(String error) {
							uploadButton.setEnabled(true);
							errorPanel.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.error16()) +" "+ error)));
						}
					});
				}
			}
		});

		add(fileInputWidget.asWidget());
		add(uploadButton);
		add(errorPanel);
	}
	
	public boolean validateSelectedFile() {
		FileMetadata[] meta = fileInputWidget.getSelectedFileMetadata();
		if(meta == null || meta.length != 1){
			showErrorMessage("Please select a file and try again");
			return false;
		}
		return true;
	}
	
	@Override
	public void showNote(String note) {
		uploadNotePanel.add(new HTML(note));
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public String getFileName() {
		return uploadedFilename;
	}
	
	@Override
	public void clear() {
		super.clear();
		uploadNotePanel.clear();
		errorPanel.clear();
	}
	/*
	 * Private Methods
	 */

}
