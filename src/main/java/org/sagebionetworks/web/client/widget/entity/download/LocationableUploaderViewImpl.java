package org.sagebionetworks.web.client.widget.entity.download;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
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
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	private TabPanel tabPanel;
	private FormPanel formPanel;
	private Button uploadBtn;
	private Button cancelBtn;
	private ProgressBar progressBar;
	private SelectionListener<ButtonEvent> uploadListener;
	private SelectionListener<ButtonEvent> cancelListener;	
	private Listener<FormEvent> submitListener;	
	private FileUploadField fileUploadField;
	
	private Radio openRadio;
	private Radio restrictedRadio;
	private MultiField buttonField;

	private boolean isInitiallyRestricted;
	
	// from http://stackoverflow.com/questions/3907531/gwt-open-page-in-a-new-tab
	private JavaScriptObject window;

	@Inject
	public LocationableUploaderViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.setLayout(new FitLayout());
		this.synapseJSNIUtils=synapseJSNIUtils;
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
	public void createUploadForm(boolean showCancel) {						
		isInitiallyRestricted = presenter.isRestricted();

		if(tabPanel == null) {
			tabPanel = new TabPanel();		
			tabPanel.setPlain(true);
			this.add(tabPanel);			
		} else {
			tabPanel.removeAll();
		}

		TabItem tab = new TabItem(DisplayConstants.LABEL_UPLOAD_TO_SYNAPSE);
		tab.addStyleName("pad-text");		
		tab.setLayout(new FlowLayout());
		addWarningToTab(tab);
		addRadioButtonsToTab(tab);
		tab.add(createUploadPanel(showCancel));
		formPanel.setAction(presenter.getUploadActionUrl(restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED));
		tabPanel.add(tab);

		tab = new TabItem(DisplayConstants.LABEL_TO_EXTERNAL);
		tab.addStyleName("pad-text");		
		tab.add(createExternalPanel(showCancel));
		tab.disable();
		tabPanel.add(tab);
		tabPanel.recalculate();
		
		this.setSize(PANEL_WIDTH+200, PANEL_HEIGHT);
		this.layout(true);
	}

	/*
	 * Private Methods
	 */
	
	private void addWarningToTab(TabItem tabItem) {
		Label lf = new Label(DisplayConstants.FILE_DOWNLOAD_NOTE);
		lf.setWidth(PANEL_WIDTH);
		lf.setAutoHeight(true);
		tabItem.add(lf);
	}
	
	private void addRadioButtonsToTab(TabItem tabItem) {
		FormPanel radioButtonPanel = new FormPanel();
		radioButtonPanel.setHeaderVisible(false);
		radioButtonPanel.setFrame(false);
		radioButtonPanel.setButtonAlign(HorizontalAlignment.RIGHT);		
		radioButtonPanel.setBorders(false);
		radioButtonPanel.setAutoWidth(true);
		radioButtonPanel.setFieldWidth(PANEL_WIDTH);
		
		openRadio = new Radio();
		openRadio.addListener(Events.OnClick, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				uploadBtn.setEnabled(true);
				formPanel.setAction(presenter.getUploadActionUrl(restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED));
			}
		});
		radioButtonPanel.add(radioField(openRadio, new Widget[]{
				createRestrictionLabel(APPROVAL_REQUIRED.NONE)}, RESTRICTED_PARAM_OPEN, false));
		restrictedRadio = new Radio();
		restrictedRadio.addListener(Events.OnClick, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				uploadBtn.setEnabled(true);
				formPanel.setAction(presenter.getUploadActionUrl(restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED));
			}
		});
		radioButtonPanel.add(radioField(restrictedRadio, new Widget[]{
				createRestrictionLabel(APPROVAL_REQUIRED.LICENSE_ACCEPTANCE),
				createRestrictionLabel(APPROVAL_REQUIRED.ACT_APPROVAL)}, RESTRICTED_PARAM_RESTRICTED, isInitiallyRestricted));

		openRadio.setValue(false);
		restrictedRadio.setValue(isInitiallyRestricted);
		
		if (isInitiallyRestricted) {
			openRadio.setEnabled(false);
			restrictedRadio.setEnabled(false);
		}

		tabItem.add(radioButtonPanel);
	}
	
	private static final int PANEL_HEIGHT = 100;
	private static final int PANEL_WIDTH = 350;
	
	private static final String RESTRICTED_PARAM_NAME = "restrictionSetting";
	private static final String RESTRICTED_PARAM_OPEN = "open";
	private static final String RESTRICTED_PARAM_RESTRICTED = "restricted";
	
	private Field radioField(Radio radio, Widget[] labels, String valueAttribute, boolean isSet) {
		MultiField mf = new MultiField();
		// maybe use mf.setLayoutData()
		radio.setHideLabel(true);
		radio.setValue(isSet);
		radio.setName(RESTRICTED_PARAM_NAME);
		radio.setValueAttribute(valueAttribute);
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
		if (openRadio.getValue()) return RADIO_SELECTED.OPEN_RADIO_SELECTED;
		if (restrictedRadio.getValue()) return RADIO_SELECTED.RESTRICTED_RADIO_SELECTED;
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
	 
	private Widget createUploadPanel(boolean showCancel) {
		if(formPanel == null) {
			formPanel = new FormPanel();	
			formPanel.setHeaderVisible(false);
			formPanel.setFrame(false);
			formPanel.setEncoding(Encoding.MULTIPART);
			formPanel.setMethod(Method.POST);
			formPanel.setButtonAlign(HorizontalAlignment.RIGHT);		
			formPanel.setHeight(PANEL_HEIGHT);
			formPanel.setBorders(false);
			formPanel.setAutoWidth(true);
			formPanel.setFieldWidth(PANEL_WIDTH);

			fileUploadField = new FileUploadField();
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
//			LabelField lf = new LabelField(DisplayConstants.FILE_DOWNLOAD_NOTE);
//			lf.setHideLabel(true);
//			formPanel.add(lf);
			
			MultiField fileUploadMF = new MultiField();
			fileUploadMF.add(fileUploadField);
			fileUploadMF.setFieldLabel("File");
			formPanel.add(fileUploadMF);		
//			openRadio = new Radio();
//			openRadio.addListener(Events.OnClick, new Listener<BaseEvent>() {
//				@Override
//				public void handleEvent(BaseEvent be) {
//					uploadBtn.setEnabled(true);
//					formPanel.setAction(presenter.getUploadActionUrl(restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED));
//				}
//			});
//			formPanel.add(radioField(openRadio, new Widget[]{
//					createRestrictionLabel(APPROVAL_REQUIRED.NONE)}, RESTRICTED_PARAM_OPEN, false));
//			restrictedRadio = new Radio();
//			restrictedRadio.addListener(Events.OnClick, new Listener<BaseEvent>() {
//				@Override
//				public void handleEvent(BaseEvent be) {
//					uploadBtn.setEnabled(true);
//					formPanel.setAction(presenter.getUploadActionUrl(restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED));
//				}
//			});
//			formPanel.add(radioField(restrictedRadio, new Widget[]{
//					createRestrictionLabel(APPROVAL_REQUIRED.LICENSE_ACCEPTANCE),
//					createRestrictionLabel(APPROVAL_REQUIRED.ACT_APPROVAL)}, RESTRICTED_PARAM_RESTRICTED, isInitiallyRestricted));
			formPanel.layout(true);
		} else {		
			formPanel.reset();
		}
		
//		openRadio.setValue(false);
//		restrictedRadio.setValue(isInitiallyRestricted);
//		if (isInitiallyRestricted) {
//			openRadio.setEnabled(false);
//			restrictedRadio.setEnabled(false);
//		}
//
//		formPanel.setAction(presenter.getUploadActionUrl(restrictedModeChosen()==RADIO_SELECTED.RESTRICTED_RADIO_SELECTED));
						
		if(progressBar == null) {
			progressBar = new ProgressBar();
		}
		progressBar.auto();
		progressBar.updateText(DisplayConstants.LABEL_UPLOADING);					
						
		// buttons
		buttonField = new MultiField();
		buttonField.setLayoutData(new FitLayout());
		formPanel.add(buttonField);
		buttonField.setHideLabel(true);
		configureUploadButton();	
		configureCancelButton();	
		
		// submit listener
		if(submitListener != null) {
			formPanel.removeListener(Events.Submit, submitListener);
		}		
		submitListener = new Listener<FormEvent>() {
			@Override
			public void handleEvent(FormEvent be) {
				presenter.handleSubmitResult(be.getResultHtml(), isNewlyRestricted());
				// hide loading
				formPanel.remove(progressBar);
				formPanel.layout(true);
			}
		};
		
		formPanel.addListener(Events.Submit, submitListener);
		
		formPanel.layout(true);
		
		return formPanel;
	}

	private void configureUploadButton() {
		if(uploadBtn == null) {
			uploadBtn = new Button("Upload");
			AdapterField af = new AdapterField(uploadBtn);
			af.setHideLabel(true);
			buttonField.add(af);
		}				
		if(uploadListener != null) {
			uploadBtn.removeSelectionListener(uploadListener);
		}
		uploadListener = new SelectionListener<ButtonEvent>() {
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
		// don't want to enable the upload button until a radio button is selected
		if (!isInitiallyRestricted) {
			uploadBtn.setEnabled(false);
		}
		uploadBtn.addSelectionListener(uploadListener);
	}
	
	private void setUploadBtnToolTip(String message) {
		Map<String,String> optionsMap = new HashMap<String,String>();
		optionsMap.put("title", message);
		optionsMap.put("data-placement", "right");
		DisplayUtils.addTooltip(synapseJSNIUtils, uploadBtn, optionsMap);
		
	}

	private void configureCancelButton() {
		if(cancelBtn == null) {
			cancelBtn = new Button("Cancel");
			AdapterField af = new AdapterField(cancelBtn);
			af.setHideLabel(true);
			buttonField.add(af);
		}
		if(cancelListener != null) {
			cancelBtn.removeSelectionListener(cancelListener);
		}
		cancelListener = new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.closeButtonSelected();
			}
		};
		cancelBtn.addSelectionListener(cancelListener);
	}

	private Widget createExternalPanel(boolean showCancel) {
		final FormPanel formPanel = new FormPanel();
		formPanel.setHeaderVisible(false);
		formPanel.setFrame(false);
		formPanel.setButtonAlign(HorizontalAlignment.RIGHT);
		formPanel.setHeight(PANEL_HEIGHT);
		
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
				presenter.setExternalLocation(pathField.getValue());
			}
		});
		formPanel.addButton(btn);
		
		return formPanel;
	}
	
	@Override
	public void openNewTab(String url) {
		// open url using window previously created
		if (window==null) return;
		DisplayUtils.setWindowTarget(window, url);
		// only use it once
		window = null;
	}

}
