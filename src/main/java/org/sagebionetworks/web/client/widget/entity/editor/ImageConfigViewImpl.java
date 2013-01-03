package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.UploadFormPanel;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigViewImpl extends LayoutContainer implements ImageConfigView {

	private Presenter presenter;
	private UploadFormPanel uploadPanel;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private TextField<String> urlField;
	private HorizontalPanel externalLinkPanel;
	private AttachmentData uploadedAttachmentData;
	TabItem externalTab,uploadTab;
	private HTMLPanel uploadStatusPanel;
	
	TabPanel tabPanel;
	@Inject
	public ImageConfigViewImpl(SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
	}
	
	@Override
	public void initView() {
		setLayout(new FitLayout());
		externalLinkPanel = getExternalLinkPanel();
		
		tabPanel = new TabPanel();
		tabPanel.setPlain(true);
		this.add(tabPanel);
		
		uploadTab = new TabItem(DisplayConstants.IMAGE_CONFIG_UPLOAD);
		uploadTab.addStyleName("pad-text");
		uploadTab.setLayout(new FlowLayout());
		tabPanel.add(uploadTab);

		externalTab = new TabItem(DisplayConstants.IMAGE_CONFIG_FROM_THE_WEB);
		externalTab.addStyleName("pad-text");
		externalTab.setLayout(new FlowLayout());
		externalTab.add(externalLinkPanel);
		tabPanel.add(externalTab);
		
		this.setHeight(150);
		this.layout(true);
	}
	
	@Override
	public AttachmentData getUploadedAttachmentData() {
		return uploadedAttachmentData;
	}

	@Override
	public void setUploadedAttachmentData(AttachmentData uploadedAttachmentData) {
		this.uploadedAttachmentData = uploadedAttachmentData;
		if (uploadedAttachmentData != null && uploadPanel != null)
			uploadPanel.getFileUploadField().setEmptyText(uploadedAttachmentData.getName());
	}
	private HorizontalPanel getExternalLinkPanel() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		urlField = new TextField<String>();
		urlField.setAllowBlank(false);
		urlField.setRegex(WebConstants.VALID_URL_REGEX);
		urlField.getMessages().setRegexText(DisplayConstants.IMAGE_CONFIG_INVALID_URL_MESSAGE);
		Label urlLabel = new Label(DisplayConstants.IMAGE_CONFIG_URL_LABEL);
		urlLabel.setWidth(70);
		
		urlField.setWidth(245);
		hp.add(urlLabel);
		hp.add(urlField);
		hp.addStyleName("margin-top-left-10");
		return hp;
	}
	
	@Override
	public void setEntityId(String entityId) {
		uploadTab.removeAll();
		//update the uploadPanel
		uploadPanel = getUploadPanel(entityId);
		uploadTab.add(uploadPanel);
		
		this.setHeight(150);
		this.layout(true);
	}
	
	private UploadFormPanel getUploadPanel(String entityId) {
		final String baseURl = GWT.getModuleBaseURL()+"attachment";
		final String actionUrl =  baseURl+ "?" + DisplayUtils.ENTITY_PARAM_KEY + "=" + entityId + "&" + DisplayUtils.ADD_TO_ENTITY_ATTACHMENTS_PARAM_KEY + "=false";
		uploadPanel = AddAttachmentDialog.getUploadFormPanel(actionUrl,sageImageBundle,DisplayConstants.ATTACH_IMAGE_DIALOG_BUTTON_TEXT,25,new AddAttachmentDialog.Callback() {
			@Override
			public void onSaveAttachment(UploadResult result) {
				if(result != null){
					if (uploadStatusPanel != null)
						uploadTab.remove(uploadStatusPanel);
					if(UploadStatus.SUCCESS == result.getUploadStatus()){
						//save close this dialog with a save
						uploadStatusPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.checkGreen16()) +" "+ DisplayConstants.UPLOAD_SUCCESSFUL_STATUS_TEXT));
						presenter.setName(result.getAttachmentData().getName());
					}else{
						uploadStatusPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.error16()) +" "+ result.getMessage()));
					}
					uploadStatusPanel.addStyleName("margin-left-180");
					uploadTab.add(uploadStatusPanel);
					layout(true);
				}
				uploadedAttachmentData = result.getAttachmentData();
			}
		}, null);
		return uploadPanel;
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (isExternal()) {
			if (!urlField.isValid())
				throw new IllegalArgumentException(urlField.getErrorMessage());
		} else {
			//attachment must have been uploaded
			if (uploadedAttachmentData == null)
				throw new IllegalArgumentException(DisplayConstants.IMAGE_CONFIG_UPLOAD_FIRST_MESSAGE);
		}
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
		return 150;
	}
	@Override
	public int getAdditionalWidth() {
		return 130;
	}
	
	@Override
	public String getImageUrl() {
		return urlField.getValue();
	}
	@Override
	public void setImageUrl(String url) {
		urlField.setValue(url);
	}
	
	@Override
	public boolean isExternal() {
		return externalTab.equals(tabPanel.getSelectedItem());
	}
	
	@Override
	public void setExternalVisible(boolean visible) {
		externalTab.setEnabled(visible);
	}
	/*
	 * Private Methods
	 */

}
