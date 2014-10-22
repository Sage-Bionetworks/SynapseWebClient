package org.sagebionetworks.web.client.widget.entity.download;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TabContent;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
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
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
/**
 * Note on the form submission.  This supports two form submission use cases.
 * 1.  Submit to Portal servlet.  These will return the response html in the SubmitCompleteHandler.
 * 2.  Submit to SFTP proxy servlet.  This will not return the response html in the SubmitCompleteHandler (due to CORS).  But the sftp proxy will return a page that sends a message (via postMessage)
 * 	to the parent window.  So we set up a listener (onAttach) for this cross-window message.
 * @author jayhodgson
 *
 */
public class UploaderViewImpl extends FlowPanel implements
		UploaderView {
	
	private boolean showCancelButton = true;
	private boolean multipleFileUploads = true;
	private boolean isExternal;
	
	public static final String FILE_FIELD_ID = "fileToUpload";
	public static final String FILE_FIELD_STYLENAME = "dragAndDropUploadBox";
	public static final String FILE_FIELD_DROP_STYLE_NAME = "dropable";
	public static final String FILE_UPLOAD_LABEL_STYLENAME = "fileUploadLabel";
	public static final int BUTTON_HEIGHT_PX = 25;
	public static final int BUTTON_WIDTH_PX = 100;

	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	private SageImageBundle sageImageBundle;
	private Dialog dialog;
	
	TextBox pathField, nameField;
	
	// initialized in constructor
	private boolean isEntity;
	private String parentEntityId;
	private FormPanel formPanel;
	private FlowPanel uploadDestinationContainer;
	
	private Form externalLinkFormPanel;
	private FormGroup externalUrlFormGroup;
	
	private FlowPanel uploadPanel;
	
	private Button uploadBtn;
	private Button cancelBtn; 
	private Progress progressContainer;
	private ProgressBar progressBar;

	// external link panel
	
	private HTML spinningProgressContainer;
	private HTML fileUploadHTML;
	private static final HTML DRAG_AND_DROP_HTML = new HTML("<p class=\"" + FILE_UPLOAD_LABEL_STYLENAME + "\">" + "or<br>Drag & Drop" + "</p>");
	FlowPanel container;
	SharingAndDataUseConditionWidget sharingDataUseWidget;
	PortalGinInjector ginInjector;
	private HandlerRegistration messageHandler;
	
	@Inject
	public UploaderViewImpl(SynapseJSNIUtils synapseJSNIUtils, 
			SageImageBundle sageImageBundle,
			SharingAndDataUseConditionWidget sharingDataUseWidget,
			PortalGinInjector ginInjector,
			Dialog dialog) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.sageImageBundle = sageImageBundle;
		this.sharingDataUseWidget = sharingDataUseWidget;
		this.ginInjector = ginInjector;
		this.dialog = dialog;
		dialog.setSize(ModalSize.MEDIUM);
		
		this.progressContainer = new Progress();
		progressContainer.setMarginTop(10);
		this.progressBar = new ProgressBar();
		progressBar.setType(ProgressBarType.INFO);
		progressContainer.add(progressBar);
		
		this.formPanel = new FormPanel();
		this.externalLinkFormPanel = new Form();
		
		spinningProgressContainer = new HTML();
		
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

					presenter.setExternalFilePath(pathField.getValue(), nameField.getValue());
				} else {
					fileUploadHTML.setVisible(false);
					DRAG_AND_DROP_HTML.setVisible(false);
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
	
	private static native String _getMessage(JavaScriptObject event) /*-{
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
		if (messageHandler != null)
			messageHandler.removeHandler();
		super.onDetach();
	}
	
	@Override
	public void resetToInitialState() {
		hideLoading();
		uploadBtn.setEnabled(true);
		fileUploadHTML.setVisible(true);
		DRAG_AND_DROP_HTML.setVisible(true);
		// Clear previously selected files.
		fileUploadHTML.setHTML(createFileUploadHTML().toString());
	}
	
	@Override
	public void showNoFilesSelectedForUpload() {
		showErrorMessage(DisplayConstants.NO_FILES_SELECTED_FOR_UPLOAD_MESSAGE);
		hideLoading();
		resetToInitialState();
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
		synapseJSNIUtils.addDropZoneStyleEventHandling(FILE_FIELD_ID, (Uploader) presenter);
	}

	@Override
	public void showErrorMessage(String message) {
		SafeHtml html = DisplayUtils.getPopupSafeHtml("", message, DisplayUtils.MessagePopup.WARNING);
		dialog.configure(DisplayConstants.UPLOAD_DIALOG_TITLE, new HTMLPanel(html.asString()), DisplayConstants.OK, null, null, true);
		dialog.show();
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
	public void disableMultipleFileUploads() {
		this.multipleFileUploads = false;
		fileUploadHTML.setHTML(createFileUploadHTML().toString());
	}
	

	/*
	 * Private Methods
	 */	
	private void createUploadContents() {
		if (container == null)
			this.container = new FlowPanel();
		else
			container.clear();
		
		container.add(dialog);	// Put modal on uploader layer.
		
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
		SafeHtml html = DisplayUtils.getPopupSafeHtml("", message, DisplayUtils.MessagePopup.QUESTION);
		dialog.configure(DisplayConstants.UPLOAD_DIALOG_TITLE, new HTMLPanel(html.asString()), DisplayConstants.YES, DisplayConstants.NO, new Dialog.Callback() {

			@Override
			public void onPrimary() {
				yesCallback.invoke();
			}

			@Override
			public void onDefault() {
				noCallback.invoke();
			}
			
		}, true);
		dialog.show();
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
		fileUploadHTML = createFileUploadHTML();
		formPanel.add(fileUploadHTML);
		configureUploadButton(); // upload tab first by default
		
		uploadPanel = new FlowPanel();
		uploadPanel.add(uploadDestinationContainer);
		uploadPanel.add(DRAG_AND_DROP_HTML);
		uploadPanel.add(formPanel);
		
		Row row = new Row();
		Column col = new Column(ColumnSize.XS_12);
		col.add(spinningProgressContainer);
		col.add(progressContainer);
		row.add(col);
		uploadPanel.add(row);
	}
	
	@Override
	public void showUploadingToExternalStorage(String url, String banner) {
		uploadDestinationContainer.clear();
		uploadDestinationContainer.add(new HTML(DisplayConstants.UPLOAD_DESTINATION + "<strong>" + SafeHtmlUtils.htmlEscape(url) + "</strong>"));
		if (banner != null)
			uploadDestinationContainer.add(new HTML(SafeHtmlUtils.htmlEscape(banner)));
	}
	
	@Override
	public void showUploadingToSynapseStorage(String banner) {
		uploadDestinationContainer.clear();
		uploadDestinationContainer.add(new InlineHTML(DisplayConstants.UPLOAD_DESTINATION));
		Image icon = new Image(sageImageBundle.logoHeader().getURL());
		icon.addStyleName("displayInline moveup-2");
		icon.setPixelSize(25, 25);
		uploadDestinationContainer.add(icon);
		icon = new Image(sageImageBundle.synapseTextHeader().getURL());
		icon.setPixelSize(88, 20);
		icon.addStyleName("displayInline margin-right-5");
		uploadDestinationContainer.add(icon);
		uploadDestinationContainer.add(new InlineHTML(" storage"));
		if (banner != null)
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
		
		FormGroup fg = new FormGroup();
		l = new FormLabel();
		l.setText("Name (Optional)");
		fg.add(l);
		fg.add(nameField);
		set.add(fg);
		
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
		uploadBtn.setText("Upload");
	}

	private void configureUploadButtonForExternal() {
		isExternal = true;
		uploadBtn.setText("Save");
	}
	
	private HTML createFileUploadHTML() {
		if (multipleFileUploads)
			return new HTML("<input id=\"" + FILE_FIELD_ID + "\" name=\"uploads[]\" type=\"file\" class=\"" + FILE_FIELD_STYLENAME + "\" multiple></input>");
		else
			return new HTML("<input id=\"" + FILE_FIELD_ID + "\" name=\"uploads[]\" type=\"file\" class=\"" + FILE_FIELD_STYLENAME + "\" /></input>");
	}

}
