package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.UploadFormPanel;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentConfigViewImpl extends LayoutContainer implements AttachmentConfigView {

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
		setLayout(new FitLayout());
		uploadedFileHandleName = null;
				
		this.setHeight(150);
		this.layout(true);
	}
	
	
	@Override
	public String getUploadedFileHandleName() {
		return uploadedFileHandleName;
	}

	@Override
	public void configure(WikiPageKey wikiKey) {
		//update the uploadPanel
		initUploadPanel(wikiKey);
		
		this.layout(true);
	}
	
	private void initUploadPanel(WikiPageKey wikiKey) {
		removeAll();
		String wikiIdParam = wikiKey.getWikiPageId() == null ? "" : "&" + DisplayUtils.WIKI_ID_PARAM_KEY + "=" + wikiKey.getWikiPageId();
		String baseURl = GWT.getModuleBaseURL()+"filehandle?" +
				DisplayUtils.WIKI_OWNER_ID_PARAM_KEY + "=" + wikiKey.getOwnerObjectId() + "&" +
				DisplayUtils.WIKI_OWNER_TYPE_PARAM_KEY + "=" + wikiKey.getOwnerObjectType() + 
				wikiIdParam;
		
		uploadPanel = AddAttachmentDialog.getUploadFormPanel(baseURl, sageImageBundle, DisplayConstants.IMAGE_CONFIG_UPLOAD, 25, new AddAttachmentDialog.Callback() {
			@Override
			public void onSaveAttachment(UploadResult result) {
				if(result != null){
					if(UploadStatus.SUCCESS == result.getUploadStatus()){
						//save close this dialog with a save
						uploadStatusPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.checkGreen16()) +" "+ DisplayConstants.UPLOAD_SUCCESSFUL_STATUS_TEXT));
					}else{
						uploadStatusPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.error16()) +" "+ result.getMessage()));
					}
					uploadStatusPanel.addStyleName("margin-left-180");
					add(uploadStatusPanel);
					layout(true);
				}
				uploadedFileHandleName = uploadPanel.getFileUploadField().getValue();
			}
		}, null);
		add(uploadPanel);
		layout(true);
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
	public void clear() {
	}
	@Override
	public int getDisplayHeight() {
		return 130;
	}
	@Override
	public int getAdditionalWidth() {
		return 90;
	}
	
	/*
	 * Private Methods
	 */

}
