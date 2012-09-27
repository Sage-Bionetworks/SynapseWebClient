package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.APPROVAL_REQUIRED;
import org.sagebionetworks.web.client.widget.entity.EntityViewUtils;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
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
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LocationableUploaderViewImpl extends LayoutContainer implements
		LocationableUploaderView {

	private Presenter presenter;

	// initialized in constructor
	private boolean isInitiallyRestricted;
	private Radio fileUploadOpenRadio;
	private Radio fileUploadRestrictedRadio;
	private Radio linkExternalOpenRadio;
	private Radio linkExternalRestrictedRadio;
	private FormPanel formPanel;
	private FileUploadField fileUploadField;
	private Button uploadBtn;
	private Button cancelBtn;
	private ProgressBar progressBar;
	
	// from http://stackoverflow.com/questions/3907531/gwt-open-page-in-a-new-tab
	private JavaScriptObject window;

	@Inject
	public LocationableUploaderViewImpl() {
		// initialize graphic elements
		this.fileUploadOpenRadio = new Radio();
		this.fileUploadRestrictedRadio = new Radio();	
		this.linkExternalOpenRadio = new Radio();
		this.linkExternalRestrictedRadio = new Radio();
		this.uploadBtn = new Button("Upload");
		this.cancelBtn = new Button("Cancel");
		this.progressBar = new ProgressBar();
		this.formPanel = new FormPanel();
		this.fileUploadField = new FileUploadField();

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
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}
	
	@Override
	public void createUploadForm() {
		initializeControls();
		
		this.removeAll();
		setLayout(new FitLayout());
		
		TabPanel tabPanel = new TabPanel();		
		tabPanel.setPlain(true);
		this.add(tabPanel);
		
		TabItem tab = new TabItem(DisplayConstants.LABEL_UPLOAD_TO_SYNAPSE);
		tab.addStyleName("pad-text");		
		tab.setLayout(new FlowLayout());
		addWarningToTab(tab);
		addRadioButtonsToLayoutContainer(tab, fileUploadOpenRadio, fileUploadRestrictedRadio);
		tab.add(formPanel);
		tabPanel.add(tab);

		tab = new TabItem(DisplayConstants.LABEL_TO_EXTERNAL);
		tab.addStyleName("pad-text");		
		tab.setLayout(new FlowLayout());
		addWarningToTab(tab);
		addRadioButtonsToLayoutContainer(tab, linkExternalOpenRadio, linkExternalRestrictedRadio);
		tab.add(createExternalPanel());
		
		tabPanel.add(tab);
		tabPanel.recalculate();
		
		this.setSize(PANEL_WIDTH+200, PANEL_HEIGHT);
		this.layout(true);
	}

	
	@Override
	public void openNewBrowserTab(String url) {
		// open url using window previously created
		if (window==null) return;
		DisplayUtils.setWindowTarget(window, url);
		// only use it once
		window = null;
	}

	/*
	 * Private Methods
	 */
	
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
		uploadBtn.setEnabled(true);
		formPanel.setAction(presenter.getUploadActionUrl(/*isRestricted*/false));		
	}
	
	private void restrictedSelected() {
		uploadBtn.setEnabled(true);
		formPanel.setAction(presenter.getUploadActionUrl(/*isRestricted*/true));		
	}
	
	// set the initial state of the controls when widget is made visible
	private void initializeControls() {
		isInitiallyRestricted = presenter.isRestricted();
		
		// radio buttons
		initializeOpenRadio(fileUploadOpenRadio, FILE_UPLOAD_RESTRICTED_PARAM_NAME, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				openSelected();
				// select other open radio button
				linkExternalOpenRadio.setValue(true);
			}
		});
		initializeRestrictedRadio(fileUploadRestrictedRadio, FILE_UPLOAD_RESTRICTED_PARAM_NAME, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				restrictedSelected();
				// select other restricted radio button
				linkExternalRestrictedRadio.setValue(true);
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
				restrictedSelected();
				// select other restricted radio button
				fileUploadRestrictedRadio.setValue(true);
			}
		});
		
		formPanel.removeAllListeners();
		Listener<FormEvent> submitListener = new Listener<FormEvent>() {
			@Override
			public void handleEvent(FormEvent be) {
				presenter.handleSubmitResult(be.getResultHtml(), isNewlyRestricted());
				// hide loading
				formPanel.remove(progressBar);
				formPanel.layout(true);
			}
		};
		formPanel.addListener(Events.Submit, submitListener);
		formPanel.setAction(presenter.getUploadActionUrl(restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED));
		fileUploadField.clearState(); // doesn't successfully clear previous selection
		//fileUploadField.clear(); // this just breaks everything!

		uploadBtn.removeAllListeners();
		SelectionListener<ButtonEvent> uploadListener = new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (!formPanel.isValid()) {
					return;
				}		
				formPanel.add(progressBar);
				formPanel.layout(true);
				// this is used in the 'handleEvent' listener, but must
				// be created in the original thread.  for more, see
				// from http://stackoverflow.com/questions/3907531/gwt-open-page-in-a-new-tab
				if (isNewlyRestricted()) {
					window = DisplayUtils.newWindow("", "", "");
				}
				formPanel.submit();
			}
		};
		uploadBtn.addSelectionListener(uploadListener);	
		// don't want to enable the upload button until a radio button is selected
		if (!isInitiallyRestricted) {
			uploadBtn.setEnabled(false);
		}
	
		cancelBtn.removeAllListeners();
		SelectionListener<ButtonEvent> cancelListener = new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.closeButtonSelected();
			}
		};
		cancelBtn.addSelectionListener(cancelListener);
	}

	private static void addWarningToTab(TabItem tabItem) {
		Label lf = new Label(DisplayConstants.FILE_DOWNLOAD_NOTE);
		lf.setWidth(PANEL_WIDTH);
		lf.setAutoHeight(true);
		tabItem.add(lf);
	}
	
	private void addRadioButtonsToLayoutContainer(
			LayoutContainer layoutContainer,
			Radio openRadio,
			Radio restrictedRadio) {
		FormPanel radioButtonPanel = new FormPanel();
		radioButtonPanel.setHeaderVisible(false);
		radioButtonPanel.setFrame(false);
		radioButtonPanel.setButtonAlign(HorizontalAlignment.RIGHT);		
		radioButtonPanel.setBorders(false);
		radioButtonPanel.setAutoWidth(true);
		radioButtonPanel.setFieldWidth(PANEL_WIDTH);
		
		radioButtonPanel.add(radioField(openRadio, new Widget[]{
				createRestrictionLabel(APPROVAL_REQUIRED.NONE)}));
		radioButtonPanel.add(radioField(restrictedRadio, new Widget[]{
				createRestrictionLabel(APPROVAL_REQUIRED.LICENSE_ACCEPTANCE),
				createRestrictionLabel(APPROVAL_REQUIRED.ACT_APPROVAL)}));

		layoutContainer.add(radioButtonPanel);
	}
	
	private static final int PANEL_HEIGHT = 100;
	private static final int PANEL_WIDTH = 350;
	
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
	private boolean isNewlyRestricted() {
		return !isInitiallyRestricted && restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED;
	}
	
	private static Widget createRestrictionLabel(APPROVAL_REQUIRED restrictionLevel) {
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant(
				"<div class=\"left "+EntityViewUtils.shieldStyleName(restrictionLevel)+
				"\"  style=\"margin-left: 7px;\"></div> <h5 class=\"left\" style=\"margin-right: 5px; margin-left: 7px;\">"+
				EntityViewUtils.restrictionDescriptor(restrictionLevel)+"</h5>");
		return new HTML(shb.toSafeHtml());
	}
	
	private static final String OR_HTML = "<h5 class=\"left\" style=\"margin-right: 5px; margin-left: 7px;\">or</h5>";
	 
	private Widget createUploadPanel() {
		formPanel.setHeaderVisible(false);
		formPanel.setFrame(false);
		formPanel.setEncoding(Encoding.MULTIPART);
		formPanel.setMethod(Method.POST);
		formPanel.setButtonAlign(HorizontalAlignment.RIGHT);		
		formPanel.setHeight(PANEL_HEIGHT);
		formPanel.setBorders(false);
		formPanel.setAutoWidth(true);
		formPanel.setFieldWidth(PANEL_WIDTH);

		fileUploadField.setWidth(PANEL_WIDTH-100);
		fileUploadField.setAllowBlank(false);
		fileUploadField.setName("uploadedfile");
		fileUploadField.setFieldLabel("File");
		
		fileUploadField.addListener(Events.OnChange, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				final String fullPath = fileUploadField.getValue();
				final int lastIndex = fullPath.lastIndexOf('\\');
				final String fileName = fullPath.substring(lastIndex + 1);
				fileUploadField.setValue(fileName);
			}
		});
		
		MultiField fileUploadMF = new MultiField();
		fileUploadMF.add(fileUploadField);
		fileUploadMF.setFieldLabel("File");
		formPanel.add(fileUploadMF);		
		formPanel.layout(true);
		
		progressBar.auto();
		progressBar.updateText(DisplayConstants.LABEL_UPLOADING);					
						
		// buttons
		MultiField buttonField = new MultiField();
		buttonField.setLayoutData(new FitLayout());
		formPanel.add(buttonField);
		buttonField.setHideLabel(true);
		AdapterField uploadAf = new AdapterField(uploadBtn);
		uploadAf.setHideLabel(true);
		buttonField.add(uploadAf);
		AdapterField cancelAf = new AdapterField(cancelBtn);
		cancelAf.setHideLabel(true);
		buttonField.add(cancelAf);
				
		formPanel.layout(true);
		
		return formPanel;
	}

	private Widget createExternalPanel() {
		final FormPanel formPanel = new FormPanel();
		formPanel.setHeaderVisible(false);
		formPanel.setFrame(false);
		formPanel.setButtonAlign(HorizontalAlignment.RIGHT);
		formPanel.setLabelWidth(110);
		formPanel.setFieldWidth(230);
		final TextField<String> pathField = new TextField<String>();
		pathField.setFieldLabel("External Path or URL");
		
		formPanel.add(pathField);
		
		Button btn = new Button("Save");
		btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (!formPanel.isValid()) {
					return;
				}				
				presenter.setExternalLocation(pathField.getValue(), isNewlyRestricted());
			}
		});
		formPanel.addButton(btn);
		
		return formPanel;
	}
}
