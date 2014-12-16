package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.dialog.UploadFormPanel;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigViewImpl implements ImageConfigView {
	public interface ImageConfigViewImplUiBinder extends UiBinder<Widget, ImageConfigViewImpl> {}
	private Widget widget;
	
	private Presenter presenter;
	SageImageBundle sageImageBundle;
	EntityFinder entityFinder;
	ClientCache clientCache;
	SynapseJSNIUtils synapseJSNIUtils;
	private UploadFormPanel uploadPanel;
	private IconsImageBundle iconsImageBundle;
	private TextField<String> urlField, nameField, entityField;
	
	@UiField
	SimplePanel externalTab;
	@UiField
	SimplePanel uploadTab;
	@UiField
	SimplePanel synapseTab;
	@UiField
	TabListItem uploadTabListItem;
	@UiField
	TabListItem externalTabListItem;
	@UiField
	TabListItem synapseTabListItem;
	@UiField
	FlowPanel uploadSuccessUI;
	@UiField
	FlowPanel uploadFailureUI;
	@UiField
	Text uploadErrorText;
	
	private String uploadedFileHandleName;
	
	private ImageParamsPanel uploadParamsPanel, synapseParamsPanel;
	
	@Inject
	public ImageConfigViewImpl(
			ImageConfigViewImplUiBinder binder,
			IconsImageBundle iconsImageBundle, SageImageBundle sageImageBundle, EntityFinder entityFinder, ClientCache clientCache, SynapseJSNIUtils synapseJSNIUtils) {
		widget = binder.createAndBindUi(this);
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		this.entityFinder = entityFinder;
		this.clientCache = clientCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	@Override
	public void initView() {
		uploadedFileHandleName = null;
		VerticalPanel externalLinkPanel = new VerticalPanel();
		externalLinkPanel.add(getExternalLinkPanel());
		externalLinkPanel.add(getExternalAltTextPanel());
		externalTab.add(externalLinkPanel);
		
		FlowPanel synapseEntityPanel = new FlowPanel();
		synapseEntityPanel.add(getSynapseEntityPanel());
		synapseParamsPanel = new ImageParamsPanel();
		synapseEntityPanel.add(synapseParamsPanel);
		
		synapseTab.add(synapseEntityPanel);
		
		uploadSuccessUI.setVisible(false);
		uploadFailureUI.setVisible(false);
	}
	
	private FormPanel getSynapseEntityPanel() {
		final FormPanel panel = new FormPanel();
		panel.setHeaderVisible(false);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setShadow(false);
		panel.setLabelAlign(LabelAlign.RIGHT);
		panel.setBodyBorder(false);
		panel.setLabelWidth(88);
		FormData basicFormData = new FormData();
		basicFormData.setWidth(330);
		Margins margins = new Margins(10, 10, 0, 0);
		basicFormData.setMargins(margins);
		
		entityField = new TextField<String>(); 
		entityField.setFieldLabel(DisplayConstants.IMAGE_FILE_ENTITY);
		entityField.setAllowBlank(false);
		entityField.setRegex(WebConstants.VALID_ENTITY_ID_REGEX);
		entityField.getMessages().setRegexText(DisplayConstants.INVALID_SYNAPSE_ID_MESSAGE);
		
		panel.add(entityField, basicFormData);
		Button findEntitiesButton = new Button(DisplayConstants.FIND_IMAGE_ENTITY, AbstractImagePrototype.create(iconsImageBundle.magnify16()));
		findEntitiesButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				entityFinder.configure(false, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {
							entityField.setValue(DisplayUtils.createEntityVersionString(selected));
							entityFinder.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});
				entityFinder.show();
			}
		});
		AdapterField buttonField = new AdapterField(findEntitiesButton);
		buttonField.setLabelSeparator("");
		panel.add(buttonField, basicFormData);
		return panel;
	}
	
	@Override
	public String getUploadedFileHandleName() {
		return uploadedFileHandleName;
	}
	
	@Override
	public void setUploadedFileHandleName(String uploadedFileHandleName) {
		this.uploadedFileHandleName = uploadedFileHandleName;
		uploadPanel.getFileUploadField().setValue(uploadedFileHandleName);
	}
	
	@Override
	public String getAlignment() {
		if (isSynapseEntity())
			return synapseParamsPanel.getAlignment();
		else
			return uploadParamsPanel.getAlignment();
	}
	
	@Override
	public String getScale() {
		if (isSynapseEntity())
			return synapseParamsPanel.getScale();
		else
			return uploadParamsPanel.getScale();
	}
	
	@Override
	public void setAlignment(String alignment) {
		if (isSynapseEntity())
			synapseParamsPanel.setAlignment(alignment);
		else
			uploadParamsPanel.setAlignment(alignment);
		
	}
	
	@Override
	public void setScale(String scale) {
		if (isSynapseEntity())
			synapseParamsPanel.setScale(scale);
		else
			uploadParamsPanel.setScale(scale);
	}
	
	private HorizontalPanel getExternalLinkPanel() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		urlField = new TextField<String>();
		urlField.setAllowBlank(false);
		urlField.setRegex(WebConstants.VALID_URL_REGEX);
		urlField.getMessages().setRegexText(DisplayConstants.IMAGE_CONFIG_INVALID_URL_MESSAGE);
		Label urlLabel = new Label(DisplayConstants.IMAGE_CONFIG_URL_LABEL);
		urlLabel.setWidth(90);
		urlField.setWidth(350);
		hp.add(urlLabel);
		hp.add(urlField);
		hp.addStyleName("margin-top-left-10");
		return hp;
	}
	
	private HorizontalPanel getExternalAltTextPanel() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		nameField = new TextField<String>();
		nameField.setAllowBlank(false);
		nameField.setRegex(WebConstants.VALID_WIDGET_NAME_REGEX);
		nameField.getMessages().setRegexText(DisplayConstants.IMAGE_CONFIG_INVALID_ALT_TEXT_MESSAGE);
		Label label = new Label(DisplayConstants.IMAGE_CONFIG_ALT_TEXT);
		label.setWidth(90);
		nameField.setWidth(350);
		hp.add(label);
		hp.add(nameField);
		hp.addStyleName("margin-top-left-10");
		return hp;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, DialogCallback dialogCallback) {
		uploadTab.clear();
		//update the uploadPanel
		initUploadPanel(wikiKey, dialogCallback);
	}
	
	private void initUploadPanel(WikiPageKey wikiKey, final DialogCallback dialogCallback) {
		
		String baseURl = GWT.getModuleBaseURL()+WebConstants.FILE_HANDLE_UPLOAD_SERVLET;
		
		//The ok/submitting button will be enabled when required images are uploaded
		//or when another tab (external or synapse) is viewed
		
		uploadPanel = AddAttachmentDialog.getUploadFormPanel(baseURl, sageImageBundle, DisplayConstants.ATTACH_IMAGE_DIALOG_BUTTON_TEXT, 25, new AddAttachmentDialog.Callback() {
			@Override
			public void onSaveAttachment(UploadResult result) {
				uploadedFileHandleName = uploadPanel.getFileUploadField().getValue();
				if(result != null){
					if(UploadStatus.SUCCESS == result.getUploadStatus()){
						uploadFailureUI.setVisible(false);
						uploadSuccessUI.setVisible(true);
						//enable the ok button
						dialogCallback.setPrimaryEnabled(true);
						presenter.addFileHandleId(result.getMessage());
						//add the local file to the client cache.  May need to fall back to the local reference in the preview (if handle has not yet been saved to the wiki)
						String fileUrl = synapseJSNIUtils.getFileUrl(AddAttachmentDialog.ATTACHMENT_FILE_FIELD_ID);
						if (fileUrl != null)
							clientCache.put(uploadedFileHandleName+WebConstants.TEMP_IMAGE_ATTACHMENT_SUFFIX, fileUrl);
					}else{
						uploadErrorText.setText(result.getMessage());
						uploadFailureUI.setVisible(true);
						uploadSuccessUI.setVisible(false);
					}
				}
			}
		}, null);
		
	    FlowPanel container = new FlowPanel();
	    container.add(uploadPanel);
	    uploadParamsPanel = new ImageParamsPanel();
	    container.add(uploadParamsPanel);
		uploadTab.add(container);
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (isExternal()) {
			if (!urlField.isValid())
				throw new IllegalArgumentException(urlField.getErrorMessage());
			if (!nameField.isValid())
				throw new IllegalArgumentException(nameField.getErrorMessage());

		} else if (isSynapseEntity()) {
			if (!entityField.isValid())
				throw new IllegalArgumentException(entityField.getErrorMessage());
		} else {
			//must have been uploaded
			if (uploadedFileHandleName == null)
				throw new IllegalArgumentException(DisplayConstants.IMAGE_CONFIG_UPLOAD_FIRST_MESSAGE);
			else {
				//block if it looks like this is not a valid image type
				String extension = uploadedFileHandleName.substring(uploadedFileHandleName.lastIndexOf(".")+1);
				if (!DisplayUtils.isRecognizedImageContentType("image/"+extension)) {
					throw new IllegalArgumentException(DisplayConstants.IMAGE_CONFIG_FILE_TYPE_MESSAGE);
				}
			}
		}
	}
	
	@Override
	public Widget asWidget() {
		return widget;
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
	public String getImageUrl() {
		return urlField.getValue();
	}
	@Override
	public String getAltText() {
		return nameField.getValue();
	}
	@Override
	public void setImageUrl(String url) {
		urlField.setValue(url);
	}
	
	@Override
	public String getSynapseId() {
		return entityField.getValue();
	}
	
	@Override
	public void setSynapseId(String synapseId) {
		entityField.setValue(synapseId);
		synapseTabListItem.showTab();
	}
	
	@Override
	public boolean isExternal() {
		return externalTabListItem.isActive();
	}
	
	
	@Override
	public boolean isSynapseEntity() {
		return synapseTabListItem.isActive();
	}
	
	@Override
	public void setExternalVisible(boolean visible) {
		externalTabListItem.setEnabled(visible);
	}
	/*
	 * Private Methods
	 */

}
