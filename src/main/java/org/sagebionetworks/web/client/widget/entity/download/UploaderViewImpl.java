package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.entity.EntityViewUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploaderViewImpl extends LayoutContainer implements
		UploaderView {

	private boolean showCancelButton = true;

	public static final String FILE_FIELD_ID = "fileToUpload";
	public static final int BUTTON_HEIGHT_PX = 25;
	public static final int BUTTON_WIDTH_PX = 100;
	private static final MarginData MARGIN = new MarginData(10);
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	private SageImageBundle sageImageBundle;
	
	TextField<String> pathField, nameField;
	
	// initialized in constructor
	private boolean isInitiallyRestricted;
	private Radio fileUploadOpenRadio;
	private Radio fileUploadRestrictedRadio;
	private Radio linkExternalOpenRadio;
	private Radio linkExternalRestrictedRadio;
	private FormPanel formPanel;
	private FileUploadField fileUploadField;
	private Button uploadBtn;
	private ProgressBar progressBar;
	// external link panel
	private String fileName;
	FormPanel externalLinkFormPanel;
	FormPanel radioButtonPanel;
	
	private HTML spinningProgressContainer;
	
	LayoutContainer container;
	IconsImageBundle iconsImageBundle;
	@Inject
	public UploaderViewImpl(SynapseJSNIUtils synapseJSNIUtils, IconsImageBundle iconsImageBundle, SageImageBundle sageImageBundle) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		
		// initialize graphic elements
		this.fileUploadOpenRadio = new Radio();
		this.fileUploadRestrictedRadio = new Radio();	
		this.linkExternalOpenRadio = new Radio();
		this.linkExternalRestrictedRadio = new Radio();
		this.uploadBtn = new Button();
		uploadBtn.setHeight(BUTTON_HEIGHT_PX);
		uploadBtn.setWidth(BUTTON_WIDTH_PX);
		uploadBtn.setEnabled(false);
		this.progressBar = new ProgressBar();
		this.formPanel = new FormPanel();
		this.fileUploadField = new FileUploadField();
		fileUploadField.setHeight(BUTTON_HEIGHT_PX);
		spinningProgressContainer = new HTML();
		// apparently the file upload dialog can only be generated once
		createUploadPanel();
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
		spinningProgressContainer = new HTML(DisplayUtils.getLoadingHtml(sageImageBundle, DisplayConstants.LABEL_INITIALIZING));
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		uploadBtn.setEnabled(false);
		fileUploadField.clear();
		pathField.clear();
		nameField.clear();
		radioButtonPanel.clear();
	}
	
	@Override
	public void createUploadForm(boolean isExternalSupported) {
		initializeControls();
		if(container == null) {
			createUploadContents(isExternalSupported);
		}

		// reset
		pathField.clear();
		nameField.clear();

	}

	
	@Override
	public int getDisplayHeight() {
		return 455;
	}

	@Override
	public int getDisplayWidth() {
		return 650;
	}
	
	@Override
	public void updateProgress(double value, String text) {
		progressBar.updateProgress(value, text);
	}
	
	@Override
	public void setShowCancelButton(boolean showCancel) {
		this.showCancelButton = showCancel;
	}

	@Override
	public void hideLoading() {
		//try to hide the loading progress bar.  ignore any errors
		progressBar.reset();
		progressBar.setVisible(false);
		spinningProgressContainer.setHTML("");
		spinningProgressContainer.setVisible(false);
	}
	
	@Override
	public void submitForm() {
		showSpinningProgress();
		spinningProgressContainer.setHTML(DisplayUtils.getLoadingHtml(sageImageBundle, DisplayConstants.LABEL_UPLOADING));
		formPanel.submit();	
	}
	

	/*
	 * Private Methods
	 */	
	private void createUploadContents(boolean isExternalSupported) {
		this.container = new LayoutContainer();
		this.setLayout(new FitLayout());
		this.addStyleName(ClientProperties.STYLE_WHITE_BACKGROUND);
		container.addStyleName(ClientProperties.STYLE_WHITE_BACKGROUND);
		container.setLayout(new FlowLayout());
		this.add(container);
				
		container.add(new HTML("<div style=\"padding: 5px 10px 0px 15px;\"><h4 class=\"" + ClientProperties.STYLE_DISPLAY_INLINE + "\">" + DisplayConstants.ACCESS_WILL_BE + ":&nbsp;</h4>" 
				+ "<div class=\"" + ClientProperties.STYLE_DISPLAY_INLINE + "\" style=\"top:-3px; position: relative;\">" + DisplayUtils.getShareSettingsDisplay(null, false, synapseJSNIUtils) + "</div>"				
				+ "</div>"));
		
		TabPanel tabPanel = new TabPanel();		
		tabPanel.setPlain(true);
		tabPanel.setHeight(100);		
		container.add(tabPanel, new MarginData(0, 10, 10, 10));
		TabItem tab;
		
		// Upload File
		tab = new TabItem(DisplayConstants.UPLOAD_FILE);
		tab.addStyleName("pad-text");
		tab.add(formPanel);
		tab.addListener(Events.Select, new Listener<TabPanelEvent>() {
            public void handleEvent( TabPanelEvent be ) {
            	configureUploadButton();
            }
        
        });
		tabPanel.add(tab);

		// External URL
		tab = new TabItem(DisplayConstants.LINK_TO_URL);
		if (!isExternalSupported)
			tab.setEnabled(false);
		tab.addStyleName("pad-text");		
		tab.add(createExternalPanel());		
		tab.addListener(Events.Select, new Listener<TabPanelEvent>() {
            public void handleEvent( TabPanelEvent be ) {
            	configureUploadButtonForExternal();
            }
        
        });
		tabPanel.add(tab);
		
		
		
		tabPanel.recalculate();

		// Data Use message 
		
		container.add(new HTML("<h3>"+ DisplayConstants.DATA_USE_BANNER +"</h3>"), new MarginData(25, 10, 5, 10));
		container.add(new HTML("<div class=\"" + ClientProperties.STYLE_DISPLAY_INLINE + "\"> <span style=\"font-size: 12pt; display: inline; color: #000;\">"
				+ DisplayConstants.DATA_USE_BANNER_SUB1  + "</span>" 				
				+ DisplayUtils.getShareSettingsDisplay(null, true, synapseJSNIUtils) 				
				+ "<span style=\"font-size: 12pt; display: inline; color: #000;\">" + DisplayConstants.DATA_USE_BANNER_SUB2 + "</span>" 				
				+"</div>"), new MarginData(0, 10, 0, 10));		
		container.add(new HTML(DisplayConstants.DATA_USE_NOTE), new MarginData(3, 10, 10, 10));
		
		addRadioButtonsToContainer(container, linkExternalOpenRadio, linkExternalRestrictedRadio);
		
		ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		bar.add(uploadBtn);
		if(showCancelButton) {
			Button cancelButton = new Button(DisplayConstants.BUTTON_CANCEL);
			cancelButton.setHeight(BUTTON_HEIGHT_PX);
			cancelButton.setWidth(BUTTON_WIDTH_PX);			
			cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					presenter.cancelClicked();
				}
			});
			bar.add(cancelButton);
		}
		container.add(bar);
		
		this.setSize(PANEL_WIDTH+200, PANEL_HEIGHT);
		container.layout(true);
		this.layout(true);
	}
	
	private void initializeOpenRadio(Radio openRadio, String radioGroup, Listener<BaseEvent> listener) {
		openRadio.removeAllListeners();
		openRadio.addListener(Events.OnClick, listener);
		openRadio.setHideLabel(true);
		openRadio.setName(radioGroup);
		openRadio.setValue(false);		
		openRadio.setEnabled(!isInitiallyRestricted);
	}
	
	private void initializeRestrictedRadio(Radio restrictedRadio, String radioGroup, Listener<BaseEvent> listener) {
		restrictedRadio.removeAllListeners();
		restrictedRadio.addListener(Events.OnClick, listener);
		restrictedRadio.setHideLabel(true);
		restrictedRadio.setName(radioGroup);
		restrictedRadio.setValue(isInitiallyRestricted);
		restrictedRadio.setEnabled(!isInitiallyRestricted);
	}
	
	private void openSelected() {
		formPanel.setAction(presenter.getDefaultUploadActionUrl(/*isRestricted*/false));		
	}
	
	private void restrictedSelected() {
		formPanel.setAction(presenter.getDefaultUploadActionUrl(/*isRestricted*/true));		
	}
	
	/**
	 * returns true if user selects "OK", false if user selects "CANCEL"
	 * @return
	 */
	private void presentRestrictedWarningDialog() {
		DisplayUtils.showOkCancelMessage(
				DisplayConstants.RESTRICTION_WARNING_TITLE, 
				DisplayConstants.RESTRICTION_WARNING, 
				MessageBox.WARNING,
				500,
				new Callback() {
					@Override
					public void invoke() {
						restrictedSelected();
						fileUploadRestrictedRadio.setValue(true);
						linkExternalRestrictedRadio.setValue(true);
					}},
				new Callback() {
					@Override
					public void invoke() {
						fileUploadRestrictedRadio.setValue(false);
						linkExternalRestrictedRadio.setValue(false);
					}}
				);
	}
	
	// set the initial state of the controls when widget is made visible
	private void initializeControls() {
		isInitiallyRestricted = presenter.isRestricted();
		
		// radio buttons
		initializeOpenRadio(fileUploadOpenRadio, FILE_UPLOAD_RESTRICTED_PARAM_NAME, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				openSelected();
				// select open radio button
				linkExternalOpenRadio.setValue(true);
			}
		});
		initializeRestrictedRadio(fileUploadRestrictedRadio, FILE_UPLOAD_RESTRICTED_PARAM_NAME, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				presentRestrictedWarningDialog();
			}
		});
		initializeOpenRadio(linkExternalOpenRadio, LINK_EXTERNAL_RESTRICTED_PARAM_NAME, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				openSelected();
				// select other open radio button
				fileUploadOpenRadio.setValue(true);
			}
		});
		initializeRestrictedRadio(linkExternalRestrictedRadio, LINK_EXTERNAL_RESTRICTED_PARAM_NAME, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				presentRestrictedWarningDialog();
			}
		});
		
		formPanel.removeAllListeners();
		Listener<FormEvent> submitListener = new Listener<FormEvent>() {
			@Override
			public void handleEvent(FormEvent be) {
				presenter.handleSubmitResult(be.getResultHtml(), isNewlyRestricted());
				hideLoading();
			}
		};
		formPanel.addListener(Events.Submit, submitListener);
		formPanel.setAction(presenter.getDefaultUploadActionUrl(restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED));
		fileUploadField.clearState(); // doesn't successfully clear previous selection
		if(formPanel.isRendered()) formPanel.reset(); // clear file choice from fileUploadField

		configureUploadButton();
		progressBar.setVisible(false);
		formPanel.add(spinningProgressContainer);
		formPanel.add(progressBar);
		formPanel.layout(true);
	}
	
	private void initializeProgress() {
		showSpinningProgress();
		spinningProgressContainer.setHTML(DisplayUtils.getLoadingHtml(sageImageBundle, DisplayConstants.LABEL_INITIALIZING));
	}
	
	@Override
	public void showProgressBar() {
		progressBar.setVisible(true);
		progressBar.reset();
		spinningProgressContainer.setVisible(false);
	}
	
	private void showSpinningProgress() {
		spinningProgressContainer.setVisible(true);
		progressBar.reset();
		progressBar.setVisible(false);
	}
	
	private void addRadioButtonsToContainer(
			LayoutContainer layoutContainer,
			Radio openRadio,
			Radio restrictedRadio) {
		radioButtonPanel = new FormPanel();
		radioButtonPanel.setHeaderVisible(false);
		radioButtonPanel.setFrame(false);
		radioButtonPanel.setButtonAlign(HorizontalAlignment.RIGHT);		
		radioButtonPanel.setBorders(false);
		radioButtonPanel.setAutoWidth(true);
		radioButtonPanel.setFieldWidth(PANEL_WIDTH);
				
		radioButtonPanel.add(radioField(openRadio, new Widget[]{
				createRestrictionLabel(RESTRICTION_LEVEL.OPEN, iconsImageBundle)}));
		radioButtonPanel.add(radioField(restrictedRadio, new Widget[]{
				createRestrictionLabel(RESTRICTION_LEVEL.RESTRICTED, iconsImageBundle),
				createRestrictionLabel(RESTRICTION_LEVEL.CONTROLLED, iconsImageBundle)}));

		layoutContainer.add(radioButtonPanel, MARGIN);
	}
	
	private static final int PANEL_HEIGHT = 100;
	private static final int PANEL_WIDTH = 590;
	
	private static final String FILE_UPLOAD_RESTRICTED_PARAM_NAME = "fileUploadRestrictionSetting";
	private static final String LINK_EXTERNAL_RESTRICTED_PARAM_NAME = "linkExternalRestrictionSetting";
	
	private static Field radioField(Radio radio, Widget[] labels) {
		MultiField mf = new MultiField();
		boolean firstTime = true;
		mf.add(radio);
		for (Widget label : labels) {
			if (firstTime) {
				firstTime = false;
			} else {
				// add 'or'
				mf.add(new AdapterField(new HTML(OR_HTML)));
			}
			AdapterField af = new AdapterField(label);
			mf.add(af);
		}
		mf.setHideLabel(true);
		mf.setBorders(true);
		return mf;
	}
	
	private enum RADIO_SELECTED {
		NO_RADIO_SELECTED,
		OPEN_RADIO_SELECTED,
		RESTRICTED_RADIO_SELECTED
	};
	
	private RADIO_SELECTED restrictedModeChosen() {
		if (fileUploadOpenRadio.getValue()) return RADIO_SELECTED.OPEN_RADIO_SELECTED;
		if (fileUploadRestrictedRadio.getValue()) return RADIO_SELECTED.RESTRICTED_RADIO_SELECTED;
		return RADIO_SELECTED.NO_RADIO_SELECTED;
	}
	
	/**
	 * Returns true iff the dataset is NOT already restricted and it is becoming restricted
	 * @return
	 */
	public boolean isNewlyRestricted() {
		return !isInitiallyRestricted && restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED;
	}
	
	private static Widget createRestrictionLabel(RESTRICTION_LEVEL restrictionLevel, IconsImageBundle iconsImageBundle) {
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("&nbsp;"
				+ DisplayUtils.getIconHtml(EntityViewUtils.getShieldIcon(restrictionLevel, iconsImageBundle))
				+ " <h5 class=\"" + ClientProperties.STYLE_DISPLAY_INLINE +  "\">"
				+ EntityViewUtils.restrictionDescriptor(restrictionLevel)+"</h5>");
		return new HTML(shb.toSafeHtml());
	}
	
	private static final String OR_HTML = "<h5 class=\"left\" style=\"margin-right: 5px; margin-left: 7px;\">or</h5>";
	 
	private Widget createUploadPanel() {
		formPanel.setHeaderVisible(false);
		formPanel.setFrame(false);
		formPanel.setEncoding(Encoding.MULTIPART);
		formPanel.setMethod(Method.POST);
		formPanel.setButtonAlign(HorizontalAlignment.LEFT);		
		formPanel.setHeight(PANEL_HEIGHT);
		formPanel.setBorders(false);
		formPanel.setAutoWidth(true);
		formPanel.setFieldWidth(PANEL_WIDTH);

		fileUploadField.setWidth(PANEL_WIDTH-100);
		fileUploadField.setAllowBlank(false);
		fileUploadField.setName("file");
		fileUploadField.setFieldLabel("File");
		fileUploadField.addListener(Events.OnChange, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				final String fullPath = fileUploadField.getValue();
				final int lastIndex = fullPath.lastIndexOf('\\');
				fileName = fullPath.substring(lastIndex + 1);
				fileUploadField.setValue(fileName);
				fileUploadField.getFileInput().setId(FILE_FIELD_ID);
				uploadBtn.setEnabled(true);
			}
		});
		MultiField fileUploadMF = new MultiField();
		fileUploadMF.add(fileUploadField);
		fileUploadMF.setFieldLabel("File");
		formPanel.add(fileUploadMF);
		formPanel.layout(true);		
		
		configureUploadButton(); // upload tab first by default
		
		progressBar.setWidth(PANEL_WIDTH - 30);
								
		formPanel.layout(true);
		
		return formPanel;
	}

	private Widget createExternalPanel() {
		externalLinkFormPanel = new FormPanel();
		pathField = new TextField<String>();
		externalLinkFormPanel.setHeaderVisible(false);
		externalLinkFormPanel.setFrame(false);
		externalLinkFormPanel.setButtonAlign(HorizontalAlignment.LEFT);
		externalLinkFormPanel.setLabelWidth(110);
		externalLinkFormPanel.setFieldWidth(PANEL_WIDTH-150);
		pathField.setFieldLabel("URL");
		pathField.addListener(Events.KeyPress, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				uploadBtn.setEnabled(true);
			}			
		});
		
		externalLinkFormPanel.add(pathField);
		
		nameField = new TextField<String>();
		nameField.setFieldLabel("Name (Optional)");
		nameField.setAllowBlank(true);
		nameField.setRegex(WebConstants.VALID_ENTITY_NAME_REGEX);
		nameField.getMessages().setRegexText(WebConstants.INVALID_ENTITY_NAME_MESSAGE);
		
		externalLinkFormPanel.add(nameField);			
		
		return externalLinkFormPanel;
	}

	private void configureUploadButton() {
		uploadBtn.setText("Upload");
		uploadBtn.removeAllListeners();
		SelectionListener<ButtonEvent> uploadListener = new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (!formPanel.isValid()) {
					return;
				}
				if(restrictedModeChosen() == RADIO_SELECTED.NO_RADIO_SELECTED) {
					showErrorMessage(DisplayConstants.SELECT_DATA_USE);
					return;
				}
				initializeProgress();
				presenter.handleUpload(fileName);
			}
		};
		uploadBtn.addSelectionListener(uploadListener);
	}

	private void configureUploadButtonForExternal() {
		uploadBtn.setText("Save");
		uploadBtn.removeAllListeners();
		uploadBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (!externalLinkFormPanel.isValid()) {
					return;
				}

				if(restrictedModeChosen() == RADIO_SELECTED.NO_RADIO_SELECTED) {
					showErrorMessage(DisplayConstants.SELECT_DATA_USE);
					return;
				}
				presenter.setExternalFilePath(pathField.getValue(), nameField.getValue(), isNewlyRestricted());
			}
		});
	}

}
