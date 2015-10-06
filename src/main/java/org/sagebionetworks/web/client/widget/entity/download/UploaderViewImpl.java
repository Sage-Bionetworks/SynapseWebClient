package org.sagebionetworks.web.client.widget.entity.download;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TabContent;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.InputType;
import org.gwtbootstrap3.client.ui.constants.ProgressBarType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EventHandlerUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.JavaScriptCallback;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidget;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
* Note on the form submission. This supports two form submission use cases.
* 1. Submit to Portal servlet. These will return the response html in the SubmitCompleteHandler.
* 2. Submit to SFTP proxy servlet. This will not return the response html in the SubmitCompleteHandler (due to CORS). But the sftp proxy will return a page that sends a message (via postMessage)
* to the parent window. So we set up a listener (onAttach) for this cross-window message.
* @author jayhodgson
*
*/
public class UploaderViewImpl extends FlowPanel implements
		UploaderView {
	
	private boolean showCancelButton = true;
	private boolean isExternal;
	
	private TextBox externalUsername;
	private PasswordTextBox externalPassword;
	
	public static final String FILE_FIELD_ID = "fileToUpload";
	public static final int BUTTON_HEIGHT_PX = 25;
	public static final int BUTTON_WIDTH_PX = 100;

	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	private SageImageBundle sageImageBundle;
	
	TextBox pathField, nameField;
	
	// initialized in constructor
	private boolean isEntity;
	private String parentEntityId;
	private FlowPanel formFieldsPanel;
	private FormPanel formPanel;
	private FlowPanel uploadDestinationContainer;
	
	private Form externalLinkFormPanel;
	private FormGroup externalUrlFormGroup;
	
	private FlowPanel uploadPanel;
	
	private Button uploadBtn, cancelBtn, chooseFileBtn;
	private Progress progressContainer;
	private ProgressBar progressBar;

	// external link panel
	
	private HTML spinningProgressContainer;
	private Input fileUploadInput;
	private Heading fileUploadLabel = new Heading(HeadingSize.H5);
	FlowPanel container;
	SharingAndDataUseConditionWidget sharingDataUseWidget;
	PortalGinInjector ginInjector;
	
	private HandlerRegistration messageHandler;
	FormGroup externalNameFormGroup;
	@Inject
	public UploaderViewImpl(SynapseJSNIUtils synapseJSNIUtils, 
			SageImageBundle sageImageBundle,
			SharingAndDataUseConditionWidget sharingDataUseWidget,
			PortalGinInjector ginInjector) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.sageImageBundle = sageImageBundle;
		this.sharingDataUseWidget = sharingDataUseWidget;
		this.ginInjector = ginInjector;
		
		this.progressContainer = new Progress();
		progressContainer.setMarginTop(10);
		this.progressBar = new ProgressBar();
		progressBar.setType(ProgressBarType.INFO);
		progressContainer.add(progressBar);
		
		this.formPanel = new FormPanel();
		this.externalLinkFormPanel = new Form();
		
		spinningProgressContainer = new HTML();
		
		chooseFileBtn = new Button("Choose File");
		chooseFileBtn.setType(ButtonType.INFO);
		chooseFileBtn.setSize(ButtonSize.LARGE);		
		uploadBtn = new Button();
		uploadBtn.setType(ButtonType.PRIMARY);
		uploadBtn.setPull(Pull.RIGHT);
		
		cancelBtn = new Button(DisplayConstants.BUTTON_CANCEL);
		cancelBtn.setType(ButtonType.DEFAULT);
		cancelBtn.setPull(Pull.RIGHT);
		cancelBtn.setMarginRight(5);

		pathField = new TextBox();
		initUploadPanel();
		initExternalPanel();
		
		initHandlers();
	}
	
	private void initHandlers() {
		uploadBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isExternal) {
					String url = pathField.getValue();
					//let the service decide what is a valid url (now supporting sftp, and perhaps others)
					if (url == null || url.isEmpty()) {
						externalUrlFormGroup.setValidationState(ValidationState.ERROR);
						return;
					}

					presenter.setExternalFilePath(pathField.getValue(), nameField.getValue(), presenter.getStorageLocationId());
				} else {
					formFieldsPanel.setVisible(false);
					fileUploadLabel.setVisible(false);
					uploadBtn.setEnabled(false);
					initializeProgress();
					presenter.handleUploads();	
				}
				
			}
		});
		
		cancelBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.cancelClicked();
			}
		});
		
		SubmitCompleteHandler submitHandler = new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				handleSubmitResult(event.getResults());
			}
		};
		formPanel.addSubmitCompleteHandler(submitHandler);
	}
	

	private void handleSubmitResult(String result) {
		if (result != null) {
			presenter.handleSubmitResult(result);
			hideLoading();
		}
	}
	
	public static native String _getMessage(JavaScriptObject event) /*-{
		console.log("event received: "+event);
		console.log("event.data received: "+event.data);
		if (event !== undefined && event.data !== undefined)
			return event.data;
		else return null;
	}-*/;
	
	@Override
	protected void onAttach() {
		//register to listen for the "message" events
		if (messageHandler == null) {
			messageHandler = EventHandlerUtils.addEventListener("message", EventHandlerUtils.getWnd(), new JavaScriptCallback() {
				@Override
				public void invoke(JavaScriptObject event) {
					handleSubmitResult(_getMessage(event));
				}
			});
		}
		super.onAttach();
	}
	
	@Override
	protected void onDetach() {
		if (messageHandler != null) {
			messageHandler.removeHandler();
			messageHandler = null;
		}
		super.onDetach();
	}
	
	@Override
	public void resetToInitialState() {
		hideLoading();
		enableUpload();
		// Clear previously selected files.
		enableMultipleFileUploads(true);
		fileUploadInput.setValue(null);
		fileUploadLabel.setText("");
	}
	
	@Override
	public void enableUpload() {
		uploadBtn.setEnabled(true);
		formFieldsPanel.setVisible(true);
		fileUploadLabel.setVisible(true);
	}
	
	@Override
	public String getExternalUsername() {
		return externalUsername.getValue();
	}
	
	@Override
	public String getExternalPassword() {
		return externalPassword.getValue();
	}

	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void triggerUpload() {
		uploadBtn.click();
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
	public void showErrorMessage(String title, String message) {
		DisplayUtils.showErrorMessage(title, message);
	}

	@Override
	public void showLoading() {
		spinningProgressContainer = new HTML(DisplayUtils.getLoadingHtml(sageImageBundle, DisplayConstants.LABEL_INITIALIZING));
		spinningProgressContainer.addStyleName("margin-top-10");
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		super.clear();
		if (pathField != null)
			externalUrlFormGroup.setValidationState(ValidationState.NONE);
			pathField.clear();
		if (nameField != null)
			nameField.clear();
		if(externalUsername != null) {
			externalUsername.clear();
			externalUsername.setVisible(false);
			externalUsername.setPlaceholder("Username");
		}
		if(externalPassword != null) {
			externalPassword.setValue("");
			externalPassword.setVisible(false);
			externalPassword.getElement().setAttribute("placeholder", "Password");
		}
	}
	
	@Override
	public void createUploadForm(boolean isEntity, String parentEntityId) {
		this.isEntity = isEntity;
		this.parentEntityId = parentEntityId;
		initializeControls();
		
		createUploadContents();
		
		// reset
		if (pathField != null)
			pathField.clear();
		if (nameField != null)
			nameField.clear();
	}
	
	@Override
	public void updateProgress(double value, String text) {
		progressBar.setText(text);
		progressBar.setPercent(value*100);
	}
	
	@Override
	public void setShowCancelButton(boolean showCancel) {
		this.showCancelButton = showCancel;
	}

	@Override
	public void hideLoading() {
		//try to hide the loading progress bar.  ignore any errors
		resetProgress();
		progressContainer.setVisible(false);
		spinningProgressContainer.setHTML("");
		spinningProgressContainer.setVisible(false);
	}
	private void resetProgress() {
		progressBar.setPercent(0.0);
		progressBar.setText("");
	}
	
	@Override
	public void submitForm(String actionUrl) {
		showSpinningProgress();
		formPanel.setAction(actionUrl);
		spinningProgressContainer.setHTML(DisplayUtils.getLoadingHtml(sageImageBundle, DisplayConstants.LABEL_UPLOADING));
		formPanel.submit();	
	}
	
	@Override
	public void enableMultipleFileUploads(boolean isEnabled) {
		if (isEnabled)
			fileUploadInput.getElement().setAttribute("multiple", null);
		else
			fileUploadInput.getElement().removeAttribute("multiple");
	}

	/*
	 * Private Methods
	 */	
	private void createUploadContents() {
		if (container == null)
			this.container = new FlowPanel();
		else
			container.clear();
		container.add(new HTML("<div style=\"padding: 5px 10px 0px 15px;\"></div>"));
		uploadPanel.removeFromParent();
		if (isEntity) {
			//create tabs
			NavTabs tabs = new NavTabs();
			TabContent tabContent = new TabContent();
			
			//Upload File
			TabListItem tab = new TabListItem(DisplayConstants.UPLOAD_FILE);
			tab.setDataTarget("#uploadTab");
			tab.setActive(true);
			tabs.add(tab);
			TabPane tabPanel = new TabPane();
			tabPanel.setActive(true);
			tabPanel.setId("uploadTab");
			tabPanel.add(uploadPanel);
			tabContent.add(tabPanel);
			tab.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					configureUploadButton();
				}
			});
			
			//External URL
			tab = new TabListItem(DisplayConstants.LINK_TO_URL);
			tab.setDataTarget("#externalTab");
			tabs.add(tab);
			externalLinkFormPanel.removeFromParent();
			tabPanel = new TabPane();
			tabPanel.setId("externalTab");
			tabPanel.add(externalLinkFormPanel);
			tabContent.add(tabPanel);
			tab.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					configureUploadButtonForExternal();
				}
			});
			
			container.add(tabs);
			container.add(tabContent);
		} else {
			container.add(uploadPanel);
			configureUploadButton();
		}

		if (isEntity && parentEntityId != null) {
			//add sharing settings and data use conditions (associated to the parent)
			sharingDataUseWidget.configure(parentEntityId, false, null);
			container.add(sharingDataUseWidget.asWidget());
		}
		
		Row row = new Row();
		Column col = new Column(ColumnSize.XS_12);
		col.add(uploadBtn);
		if(showCancelButton) {
			col.add(cancelBtn);
		}
		row.add(col);
		container.add(row);
	}
	
	@Override
	public void showUploaderUI() {
		clear();
		add(container);
	}
	
	@Override
	public void showConfirmDialog(String message, final Callback yesCallback, final Callback noCallback) {
		DisplayUtils.showConfirmDialog(DisplayConstants.UPLOAD_DIALOG_TITLE, message, yesCallback, noCallback);
	}
	
	// set the initial state of the controls when widget is made visible
	private void initializeControls() {
		if(formPanel.isVisible()) formPanel.reset(); // clear file choice from fileUploadField

		configureUploadButton();
		progressContainer.setVisible(false);
	}
	
	private void initializeProgress() {
		showSpinningProgress();
		spinningProgressContainer.setHTML(DisplayUtils.getLoadingHtml(sageImageBundle, DisplayConstants.LABEL_INITIALIZING));
	}
	
	@Override
	public void showProgressBar() {
		resetProgress();
		progressContainer.setVisible(true);
		spinningProgressContainer.setVisible(false);
	}
	
	private void showSpinningProgress() {
		spinningProgressContainer.setVisible(true);
		resetProgress();
		progressContainer.setVisible(false);
	}
		
	private void initUploadPanel() {
		uploadDestinationContainer = new FlowPanel();
		uploadDestinationContainer.addStyleName("alert alert-info margin-5");
		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		formPanel.setMethod(FormPanel.METHOD_POST);
		FlowPanel fileInputPanel = new FlowPanel();
		fileUploadInput = new Input(InputType.FILE);
		fileUploadInput.setId(FILE_FIELD_ID);
		fileUploadInput.setName("uploads[]");
		fileUploadInput.setVisible(false);
		fileUploadLabel.addStyleName("displayInline margin-left-5");
		fileUploadInput.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				fileUploadLabel.setText(presenter.getSelectedFilesText());
				uploadBtn.click();
			}
		});
		chooseFileBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//click file upload input field
				fileUploadInput.getElement().<InputElement>cast().click();
			}
		});
		fileInputPanel.add(fileUploadInput);
		fileInputPanel.add(chooseFileBtn);
		fileInputPanel.add(fileUploadLabel);
		enableMultipleFileUploads(true);
		formFieldsPanel = new FlowPanel();
		
		externalUsername = new TextBox();
		externalUsername.addStyleName("margin-bottom-5");
		externalUsername.setName("username");
		externalPassword = new PasswordTextBox();
		externalPassword.setName("password");
		externalPassword.setStyleName("form-control margin-bottom-5");
		externalUsername.setVisible(false);
		externalPassword.setVisible(false);
		
		formFieldsPanel.add(externalUsername);
		formFieldsPanel.add(externalPassword);
		formFieldsPanel.add(fileInputPanel);
		configureUploadButton(); // upload tab first by default
		
		formPanel.setWidget(formFieldsPanel);
		uploadPanel = new FlowPanel();
		uploadPanel.add(uploadDestinationContainer);
		uploadPanel.add(formPanel);
		
		Row row = new Row();
		Column col = new Column(ColumnSize.XS_12);
		col.add(spinningProgressContainer);
		col.add(progressContainer);
		row.add(col);
		uploadPanel.add(row);
	}

	@Override
	public void showUploadingToExternalStorage(String host, String banner) {
		uploadDestinationContainer.clear();
		String escapedHost = SafeHtmlUtils.htmlEscape(host);
		uploadDestinationContainer.add(new HTML(DisplayConstants.UPLOAD_DESTINATION + "<strong>" + escapedHost + "</strong>"));
		if (banner != null)
			uploadDestinationContainer.add(new HTML(SafeHtmlUtils.htmlEscape(banner)));
		//add the host to the field names too
		externalUsername.setPlaceholder(escapedHost + " username");
		externalPassword.getElement().setAttribute("placeholder", escapedHost + " password");

		externalUsername.setVisible(true);
		externalPassword.setVisible(true);
	}
	
	@Override
	public void showUploadingToSynapseStorage() {
		uploadDestinationContainer.clear();
		uploadDestinationContainer.add(new InlineHTML(DisplayConstants.UPLOAD_DESTINATION));
		Image icon = new Image(sageImageBundle.synapseLogo().getURL());
		icon.setPixelSize(88, 20);
		icon.addStyleName("displayInline margin-right-5");
		uploadDestinationContainer.add(icon);
		uploadDestinationContainer.add(new InlineHTML(" storage"));
	}
	
	@Override
	public void showUploadingBanner(String banner) {
		uploadDestinationContainer.clear();
		uploadDestinationContainer.add(new HTML(SafeHtmlUtils.htmlEscape(banner)));
	}
	
	private void initExternalPanel() {
		pathField = new TextBox();
		nameField = new TextBox();
		
		FieldSet set = new FieldSet();
		
		externalUrlFormGroup = new FormGroup();
		FormLabel l = new FormLabel();
		l.setText("URL");
		externalUrlFormGroup.add(l);
		externalUrlFormGroup.add(pathField);
		set.add(externalUrlFormGroup);
		
		externalNameFormGroup = new FormGroup();
		l = new FormLabel();
		l.setText("Name (Optional)");
		externalNameFormGroup.add(l);
		externalNameFormGroup.add(nameField);
		set.add(externalNameFormGroup);
		
		externalLinkFormPanel.add(set);
		pathField.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				uploadBtn.setEnabled(true);
			}
		});
	}

	private void configureUploadButton() {
		isExternal = false;
		uploadBtn.setText("Save");
	}

	private void configureUploadButtonForExternal() {
		isExternal = true;
		uploadBtn.setText("Save");
	}
	
	@Override
	public void setUploaderLinkNameVisible(boolean visible) {
		externalNameFormGroup.setVisible(visible);
	}

}
