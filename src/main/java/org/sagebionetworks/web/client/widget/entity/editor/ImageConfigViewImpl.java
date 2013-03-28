package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.UploadFormPanel;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigViewImpl extends LayoutContainer implements ImageConfigView {

	private static final int DISPLAY_HEIGHT = 220;
	private Presenter presenter;
	SageImageBundle sageImageBundle;
	private UploadFormPanel uploadPanel;
	private IconsImageBundle iconsImageBundle;
	private TextField<String> urlField;
	private TextField<String> nameField;
	private HorizontalPanel externalLinkPanel;
	TabItem externalTab,uploadTab;
	private HTMLPanel uploadStatusPanel;
	private String uploadedFileHandleName;
	private Slider scaleSlider;
	private SimpleComboBox<String> alignmentCombo;
	
	TabPanel tabPanel;
	@Inject
	public ImageConfigViewImpl(IconsImageBundle iconsImageBundle, SageImageBundle sageImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
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
		
		this.setHeight(DISPLAY_HEIGHT);
		this.layout(true);
	}
	
	
	@Override
	public String getUploadedFileHandleName() {
		return uploadedFileHandleName;
	}
	@Override
	public String getAlignment() {
		if(alignmentCombo != null && alignmentCombo.getValue() != null)
			return alignmentCombo.getValue().getValue();			
		return null;
	}
	
	@Override
	public String getScale() {
		if (scaleSlider != null)
			return Integer.toString(scaleSlider.getValue());
		return null;
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
	    container.add(getImageParamsPanel());
		uploadTab.add(container);
		layout(true);
	}
	
	public FormPanel getImageParamsPanel() {
		FormPanel panel = new FormPanel();
		panel.setHeaderVisible(false);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setShadow(false);
		panel.setLabelAlign(LabelAlign.LEFT);
		panel.setBodyBorder(false);
		FormData basicFormData = new FormData("-50");
		panel.setFieldWidth(40);
		//and add scale and alignment
		scaleSlider = new Slider();
	    scaleSlider.setMinValue(1);
	    scaleSlider.setMaxValue(200);
	    scaleSlider.setValue(100);
	    scaleSlider.setIncrement(1);
	    final SliderField sf = new SliderField(scaleSlider);
	    sf.setFieldLabel("Scale (100%)");
	    //bug in gxt slider where the message popup is shown far from the slider, and can't seem to hide it
	    scaleSlider.setMessage("{0}%");
	    //update the field label as a workaround
	    scaleSlider.addListener(Events.Change, new Listener<SliderEvent>() {
	    	@Override
	    	public void handleEvent(SliderEvent be) {
	    		sf.setFieldLabel("Scale (" + be.getNewValue() + "%)");
	    	}
		});

	    panel.add(sf, basicFormData);
	    
	    alignmentCombo = new SimpleComboBox<String>();
		alignmentCombo.add(WidgetConstants.FLOAT_NONE);
		alignmentCombo.add(WidgetConstants.FLOAT_LEFT);
		alignmentCombo.add(WidgetConstants.FLOAT_RIGHT);
		alignmentCombo.setSimpleValue(WidgetConstants.FLOAT_NONE);
		alignmentCombo.setTypeAhead(false);
		alignmentCombo.setEditable(false);
		alignmentCombo.setForceSelection(true);
		alignmentCombo.setTriggerAction(TriggerAction.ALL);
		alignmentCombo.setFieldLabel("Alignment");
		
		panel.add(alignmentCombo, basicFormData);

		return panel;
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (isExternal()) {
			if (!urlField.isValid())
				throw new IllegalArgumentException(urlField.getErrorMessage());
			if (!nameField.isValid())
				throw new IllegalArgumentException(nameField.getErrorMessage());

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
