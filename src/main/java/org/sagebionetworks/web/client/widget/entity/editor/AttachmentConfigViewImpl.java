package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.dialog.UploadFormPanel;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentConfigViewImpl extends FlowPanel implements AttachmentConfigView {

	private Presenter presenter;
	SageImageBundle sageImageBundle;
	private UploadFormPanel uploadPanel;
	private IconsImageBundle iconsImageBundle;
	private HTMLPanel uploadStatusPanel;
	private String uploadedFileHandleName;
	
	@Inject
	public AttachmentConfigViewImpl(IconsImageBundle iconsImageBundle, SageImageBundle sageImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
	}
	
	@Override
	public void initView() {
		uploadedFileHandleName = null;
	}
	
	
	@Override
	public String getUploadedFileHandleName() {
		return uploadedFileHandleName;
	}
	
	@Override
	public void setUploadedFileHandleName(String fileHandleName) {
		this.uploadedFileHandleName = fileHandleName;
	}

	@Override
	public void configure(WikiPageKey wikiKey, DialogCallback dialogCallback) {
		//update the uploadPanel
		initUploadPanel(wikiKey, dialogCallback);
	}
	
	private void initUploadPanel(WikiPageKey wikiKey, final DialogCallback dialogCallback) {
		clear();
		String baseURl = GWT.getModuleBaseURL()+WebConstants.FILE_HANDLE_UPLOAD_SERVLET;
		
		//The ok/submitting button will be enabled when attachments are uploaded
		dialogCallback.setPrimaryEnabled(false);
		uploadPanel = AddAttachmentDialog.getUploadFormPanel(baseURl, DisplayConstants.IMAGE_CONFIG_UPLOAD, new AddAttachmentDialog.Callback() {
			@Override
			public void onSaveAttachment(UploadResult result) {
				if(result != null){
					if(UploadStatus.SUCCESS == result.getUploadStatus()){
						//save close this dialog with a save
						uploadStatusPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.checkGreen16()) +" "+ DisplayConstants.UPLOAD_SUCCESSFUL_STATUS_TEXT));
						//enable the ok button
						dialogCallback.setPrimaryEnabled(true);
						presenter.addFileHandleId(result.getMessage());
					}else{
						uploadStatusPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.error16()) +" "+ result.getMessage()));
					}
					uploadStatusPanel.addStyleName("margin-left-180");
					add(uploadStatusPanel);
				}
				uploadedFileHandleName = uploadPanel.getFilename();
			}
		});
		add(uploadPanel);
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		//must have been uploaded
		if (uploadedFileHandleName == null)
			throw new IllegalArgumentException(DisplayConstants.IMAGE_CONFIG_UPLOAD_FIRST_MESSAGE);
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
	public void setAccept(String acceptedMimeTypes) {
		uploadPanel.getFileUploadField().getElement().setAttribute("accept", acceptedMimeTypes);
	}
	
	@Override
	public void clear() {
	}
	/*
	 * Private Methods
	 */

}
