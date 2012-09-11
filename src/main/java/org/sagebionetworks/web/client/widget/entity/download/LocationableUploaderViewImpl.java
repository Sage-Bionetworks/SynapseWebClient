package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LocationableUploaderViewImpl extends LayoutContainer implements
		LocationableUploaderView {

	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private TabPanel tabPanel;
	private FormPanel formPanel;
	private Button uploadBtn;
	private Button cancelBtn;
	private ProgressBar progressBar;
	private SelectionListener<ButtonEvent> uploadListener;
	private SelectionListener<ButtonEvent> cancelListener;
	private Listener<FormEvent> submitListener;	
	private FileUploadField fileUploadField;
	
	
	@Inject
	public LocationableUploaderViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.setLayout(new FitLayout());
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
		if(tabPanel == null) {
			tabPanel = new TabPanel();		
			tabPanel.setPlain(true);
			this.add(tabPanel);			
		} else {
			tabPanel.removeAll();
		}

		TabItem tab = new TabItem(DisplayConstants.LABEL_UPLOAD_TO_SYNAPSE);
		tab.addStyleName("pad-text");		
		tab.add(createUploadPanel(showCancel));
		tabPanel.add(tab);

		tab = new TabItem(DisplayConstants.LABEL_TO_EXTERNAL);
		tab.addStyleName("pad-text");		
		tab.add(createExternalPanel(showCancel));
		
		tabPanel.add(tab);
		tabPanel.recalculate();
		
		tabPanel.recalculate();
		this.layout(true);
	}

	/*
	 * Private Methods
	 */

	private Widget createUploadPanel(boolean showCancel) {
		if(formPanel == null) {
			formPanel = new FormPanel();			
			formPanel.setHeaderVisible(false);
			formPanel.setFrame(false);
			formPanel.setEncoding(Encoding.MULTIPART);
			formPanel.setMethod(Method.POST);
			formPanel.setButtonAlign(HorizontalAlignment.RIGHT);		
			formPanel.setHeight(100);
			formPanel.setLabelWidth(50);
			
			fileUploadField = new FileUploadField();
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
			formPanel.add(fileUploadField);			
		} else {		
			formPanel.reset();
		}
		formPanel.setAction(presenter.getUploadActionUrl());
						
		if(progressBar == null) {
			progressBar = new ProgressBar();
		}
		progressBar.auto();
		progressBar.updateText(DisplayConstants.LABEL_UPLOADING);					
						
		// buttons
		configureUploadButton();		
		configureCancelButton();		
		
		// submit listener
		if(submitListener != null) {
			formPanel.removeListener(Events.Submit, submitListener);
		}		
		submitListener = new Listener<FormEvent>() {
			@Override
			public void handleEvent(FormEvent be) {
				presenter.handleSubmitResult(be.getResultHtml());
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
			formPanel.addButton(uploadBtn);
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
				formPanel.submit();
			}
		};
		uploadBtn.addSelectionListener(uploadListener);
	}

	private void configureCancelButton() {
		if(cancelBtn == null) {
			cancelBtn = new Button("Cancel");
			formPanel.addButton(cancelBtn);
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
		formPanel.setHeight(100);
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
				presenter.setExternalLocation(pathField.getValue());
			}
		});
		formPanel.addButton(btn);
		
		return formPanel;
	}

}
