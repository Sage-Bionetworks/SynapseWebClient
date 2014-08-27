package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidget;
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
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploaderViewImpl extends LayoutContainer implements
		UploaderView {

	private boolean showCancelButton = true;
	
	public static final String FILE_FIELD_ID = "fileToUpload";
	public static final int BUTTON_HEIGHT_PX = 25;
	public static final int BUTTON_WIDTH_PX = 100;
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	private SageImageBundle sageImageBundle;
	
	TextField<String> pathField, nameField;
	
	// initialized in constructor
	private boolean isEntity;
	private String parentEntityId;
	private FormPanel formPanel, externalLinkFormPanel;
	
	
	private FileUploadField fileUploadField;
	private Button uploadBtn;
	private Button cancelBtn; 
	private ProgressBar progressBar;
	// external link panel
	private String fileName;
	
	private HTML spinningProgressContainer;
	
	LayoutContainer container;
	SharingAndDataUseConditionWidget sharingDataUseWidget;
	PortalGinInjector ginInjector;
	
	@Inject
	public UploaderViewImpl(SynapseJSNIUtils synapseJSNIUtils, 
			SageImageBundle sageImageBundle,
			SharingAndDataUseConditionWidget sharingDataUseWidget, PortalGinInjector ginInjector) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.sageImageBundle = sageImageBundle;
		this.sharingDataUseWidget = sharingDataUseWidget;
		this.ginInjector = ginInjector;
		this.uploadBtn = new Button();
		uploadBtn.setHeight(BUTTON_HEIGHT_PX);
		uploadBtn.setWidth(BUTTON_WIDTH_PX);
		this.progressBar = new ProgressBar();
		this.formPanel = new FormPanel();
		this.fileUploadField = new FileUploadField();
		fileUploadField.setHeight(BUTTON_HEIGHT_PX);
		spinningProgressContainer = new HTML();
		// apparently the file upload dialog can only be generated once
		createUploadPanel();
		createExternalPanel();
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
		removeAll();
		if (fileUploadField.isRendered())
			fileUploadField.clear();
		if (pathField != null && pathField.isRendered())
			pathField.clear();
		if (nameField != null && nameField.isRendered())
			nameField.clear();
	}
	
	@Override
	public void createUploadForm(boolean isEntity, String parentEntityId, boolean isDirectUploadSupported) {
		this.isEntity = isEntity;
		this.parentEntityId = parentEntityId;
		initializeControls();
		
		setSize(PANEL_WIDTH, PANEL_HEIGHT);
		createUploadContents(isDirectUploadSupported);
		
		// reset
		if (pathField != null)
			pathField.clear();
		if (nameField != null)
			nameField.clear();
	}

	
	@Override
	public int getDisplayHeight() {
		return isEntity ? 350 : 200;
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
	private void createUploadContents(boolean isDirectUploadSupported) {
		if (container == null)
			this.container = new LayoutContainer();
		else
			container.removeAll();
		
		this.addStyleName(ClientProperties.STYLE_WHITE_BACKGROUND);
		container.addStyleName(ClientProperties.STYLE_WHITE_BACKGROUND);
		container.setLayout(new FlowLayout());
				
		container.add(new HTML("<div style=\"padding: 5px 10px 0px 15px;\"></div>"));
		if (isEntity) {
			TabPanel tabPanel = new TabPanel();
			tabPanel.setPlain(true);
			tabPanel.setHeight(PANEL_HEIGHT);		
			TabItem tab;
			
			// Upload File
			tab = new TabItem(DisplayConstants.UPLOAD_FILE);
			tab.addStyleName("pad-text");			
			formPanel.removeFromParent();
						
			tab.add(formPanel);			
			tab.addListener(Events.Select, new Listener<TabPanelEvent>() {
	            public void handleEvent( TabPanelEvent be ) {
	            	configureUploadButton();
	            }
	        });
		
			tabPanel.add(tab);
			tabPanel.repaint();
	
			// External URL
			tab = new TabItem(DisplayConstants.LINK_TO_URL);
			tab.addStyleName("pad-text");
			externalLinkFormPanel.removeFromParent();
			tab.add(externalLinkFormPanel);		
			tab.addListener(Events.Select, new Listener<TabPanelEvent>() {
	            public void handleEvent( TabPanelEvent be ) {
	            	configureUploadButtonForExternal();
	            }
	        
	        });
			tabPanel.add(tab);
			tabPanel.recalculate();
			container.add(tabPanel, new MarginData(0, 10, 10, 10));
		} else {
			formPanel.removeFromParent();
			container.add(formPanel);
			configureUploadButton();
		}

		if (isEntity && parentEntityId != null) {
			//add sharing settings and data use conditions (associated to the parent)
			sharingDataUseWidget.configure(parentEntityId, false, null);
			container.add(sharingDataUseWidget.asWidget());
		}
		
		ButtonBar bar = new ButtonBar();
		bar.setAlignment(HorizontalAlignment.RIGHT);
		bar.add(uploadBtn);
		if(showCancelButton) {
			cancelBtn = new Button(DisplayConstants.BUTTON_CANCEL);
			cancelBtn.setHeight(BUTTON_HEIGHT_PX);
			cancelBtn.setWidth(BUTTON_WIDTH_PX);			
			cancelBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					presenter.cancelClicked();
				}
			});
			bar.add(cancelBtn);
		}
		container.add(bar);
		container.layout(true);
	}

	@Override
	public void showUploaderUI() {
		removeAll();
		add(container);
		layout(true);
	}
	
	@Override
	public void showConfirmDialog(String title, String message, Callback yesCallback, Callback noCallback) {
		DisplayUtils.showConfirmDialog(title, message, yesCallback, noCallback);
	}
	
	// set the initial state of the controls when widget is made visible
	private void initializeControls() {
		formPanel.removeAllListeners();
		Listener<FormEvent> submitListener = new Listener<FormEvent>() {
			@Override
			public void handleEvent(FormEvent be) {
				presenter.handleSubmitResult(be.getResultHtml());
				hideLoading();
			}
		};
		formPanel.addListener(Events.Submit, submitListener);
		formPanel.setAction(presenter.getDefaultUploadActionUrl());
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
		
	private static final int PANEL_HEIGHT = 100;
	private static final int PANEL_WIDTH = 790;
	
	
	private Widget createUploadPanel() {
		formPanel.setHeaderVisible(false);
		formPanel.setFrame(false);
		formPanel.setEncoding(Encoding.MULTIPART);
		formPanel.setMethod(Method.POST);
		formPanel.setButtonAlign(HorizontalAlignment.LEFT);		
		formPanel.setAutoHeight(false);
		formPanel.setHeight(PANEL_HEIGHT);
		formPanel.setBorders(false);
		formPanel.setAutoWidth(false);
		formPanel.setWidth(PANEL_WIDTH-170);
		formPanel.setFieldWidth(PANEL_WIDTH-300);

		fileUploadField.setWidth(PANEL_WIDTH-300);
		fileUploadField.setAllowBlank(false);
		fileUploadField.setName("file");
		fileUploadField.setFieldLabel("File");
		fileUploadField.addListener(Events.OnChange, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				if(fileUploadField.getValue() == null) return;
				final String fullPath = fileUploadField.getValue();
				final int lastIndex = fullPath.lastIndexOf('\\');
				fileName = fullPath.substring(lastIndex + 1);
				fileUploadField.setValue(fileName);
				fileUploadField.getFileInput().setId(FILE_FIELD_ID);
				uploadBtn.setEnabled(true);
			}
		});
		formPanel.add(fileUploadField);
		formPanel.layout(true);		
		
		configureUploadButton(); // upload tab first by default
		
		progressBar.setWidth(PANEL_WIDTH - 230);
								
		formPanel.layout(true);
		
		return formPanel;
	}

	private Widget createExternalPanel() {
		externalLinkFormPanel = new FormPanel();
		pathField = new TextField<String>();
		externalLinkFormPanel.setHeaderVisible(false);
		externalLinkFormPanel.setAutoHeight(false);
		externalLinkFormPanel.setHeight(PANEL_HEIGHT);
		externalLinkFormPanel.setFrame(false);
		externalLinkFormPanel.setButtonAlign(HorizontalAlignment.LEFT);
		externalLinkFormPanel.setLabelWidth(110);
		externalLinkFormPanel.setBorders(false);
		externalLinkFormPanel.setFieldWidth(PANEL_WIDTH-350);
		externalLinkFormPanel.setAutoWidth(false);
		externalLinkFormPanel.setWidth(PANEL_WIDTH-270);
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
		externalLinkFormPanel.layout(true);
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
				uploadBtn.setEnabled(false);
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

				presenter.setExternalFilePath(pathField.getValue(), nameField.getValue());
			}
		});
	}
}
