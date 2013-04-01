package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.UploadFormPanel;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigViewImpl extends LayoutContainer implements ImageConfigView {

	private static final int DISPLAY_HEIGHT = 220;
	private Presenter presenter;
	SageImageBundle sageImageBundle;
	EntityFinder entityFinder;
	private UploadFormPanel uploadPanel;
	private IconsImageBundle iconsImageBundle;
	private TextField<String> urlField, nameField, entityField;
	
	TabItem externalTab,uploadTab, synapseTab;
	private HTMLPanel uploadStatusPanel;
	private String uploadedFileHandleName;
	
	private ImageParamsPanel uploadParamsPanel, synapseParamsPanel;
	
	TabPanel tabPanel;
	@Inject
	public ImageConfigViewImpl(IconsImageBundle iconsImageBundle, SageImageBundle sageImageBundle, EntityFinder entityFinder) {
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		this.entityFinder = entityFinder;
	}
	
	@Override
	public void initView() {
		setLayout(new FitLayout());
		uploadedFileHandleName = null;
		VerticalPanel externalLinkPanel = new VerticalPanel();
		externalLinkPanel.add(getExternalLinkPanel());
		externalLinkPanel.add(getExternalAltTextPanel());
		
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
		
		synapseTab = new TabItem(DisplayConstants.IMAGE_CONFIG_FROM_SYNAPSE);
		synapseTab.addStyleName("pad-text");
		synapseTab.setLayout(new FlowLayout());
		FlowPanel synapseEntityPanel = new FlowPanel();
		synapseEntityPanel.add(getSynapseEntityPanel());
		synapseParamsPanel = new ImageParamsPanel();
		synapseEntityPanel.add(synapseParamsPanel);
		
		synapseTab.add(synapseEntityPanel);
		tabPanel.add(synapseTab);
		
		this.setHeight(DISPLAY_HEIGHT);
		this.layout(true);
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
				entityFinder.configure(false);				
				final Window window = new Window();
				DisplayUtils.configureAndShowEntityFinderWindow(entityFinder, window, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {
							entityField.setValue(DisplayUtils.createEntityVersionString(selected));
							window.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});
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
	public void configure(WikiPageKey wikiKey) {
		uploadTab.removeAll();
		//update the uploadPanel
		initUploadPanel(wikiKey);
		
		this.setHeight(DISPLAY_HEIGHT);
		this.layout(true);
	}
	
	private void initUploadPanel(WikiPageKey wikiKey) {
		
		String wikiIdParam = wikiKey.getWikiPageId() == null ? "" : "&" + DisplayUtils.WIKI_ID_PARAM_KEY + "=" + wikiKey.getWikiPageId();
		String baseURl = GWT.getModuleBaseURL()+"filehandle?" +
				DisplayUtils.WIKI_OWNER_ID_PARAM_KEY + "=" + wikiKey.getOwnerObjectId() + "&" +
				DisplayUtils.WIKI_OWNER_TYPE_PARAM_KEY + "=" + wikiKey.getOwnerObjectType() + 
				wikiIdParam;
		
		uploadPanel = AddAttachmentDialog.getUploadFormPanel(baseURl, sageImageBundle, DisplayConstants.ATTACH_IMAGE_DIALOG_BUTTON_TEXT, 25, new AddAttachmentDialog.Callback() {
			@Override
			public void onSaveAttachment(UploadResult result) {
				if(result != null){
					if (uploadStatusPanel != null)
						uploadTab.remove(uploadStatusPanel);
					if(UploadStatus.SUCCESS == result.getUploadStatus()){
						//save close this dialog with a save
						uploadStatusPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.checkGreen16()) +" "+ DisplayConstants.UPLOAD_SUCCESSFUL_STATUS_TEXT));
					}else{
						uploadStatusPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.error16()) +" "+ result.getMessage()));
					}
					uploadStatusPanel.addStyleName("margin-left-180");
					uploadTab.add(uploadStatusPanel);
					layout(true);
				}
				uploadedFileHandleName = uploadPanel.getFileUploadField().getValue();
			}
		}, null);
		
	    FlowPanel container = new FlowPanel();
	    container.add(uploadPanel);
	    uploadParamsPanel = new ImageParamsPanel();
	    container.add(uploadParamsPanel);
		uploadTab.add(container);
		layout(true);
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
		return DISPLAY_HEIGHT;
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
	public boolean isExternal() {
		return externalTab.equals(tabPanel.getSelectedItem());
	}
	
	@Override
	public boolean isSynapseEntity() {
		return synapseTab.equals(tabPanel.getSelectedItem());
	}
	
	@Override
	public void setExternalVisible(boolean visible) {
		externalTab.setEnabled(visible);
	}
	/*
	 * Private Methods
	 */

}
