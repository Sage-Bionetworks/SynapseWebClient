package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentConfigViewImpl extends FlowPanel implements AttachmentConfigView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private FlowPanel errorPanel = new FlowPanel();
	private FlowPanel uploadNotePanel = new FlowPanel();
	private Widget fileInputWidget;
	Button uploadButton = new Button(DisplayConstants.IMAGE_CONFIG_UPLOAD);
	private HTMLPanel successPanel;
	
	@Inject
	public AttachmentConfigViewImpl(IconsImageBundle iconsImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
		uploadButton.setType(ButtonType.INFO);
		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.uploadFileClicked();
			}
		});
		successPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.checkGreen16()) +" "+ DisplayConstants.UPLOAD_SUCCESSFUL_STATUS_TEXT));
	}
	
	@Override
	public void initView() {
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, DialogCallback dialogCallback) {
		add(successPanel);
		add(uploadNotePanel);
		add(fileInputWidget);
		add(uploadButton);
		add(errorPanel);
		successPanel.setVisible(false);
		uploadNotePanel.setVisible(true);
		fileInputWidget.setVisible(true);
		uploadButton.setVisible(true);
		errorPanel.setVisible(true);
	}
	
	@Override
	public void setUploadButtonEnabled(boolean enabled) {
		uploadButton.setEnabled(enabled);
	}
	@Override
	public void showUploadSuccessUI() {
		successPanel.setVisible(true);
		uploadNotePanel.setVisible(false);
		fileInputWidget.setVisible(false);
		uploadButton.setVisible(false);
		errorPanel.setVisible(false);
	}
	
	@Override
	public void showUploadFailureUI(String error) {
		errorPanel.clear();
		errorPanel.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.error16()) +" "+ error)));
	}
	
	
	@Override
	public void setFileInputWidget(Widget fileInputWidget) {
		this.fileInputWidget = fileInputWidget;
	}
	
	@Override
	public void showNote(String note) {
		uploadNotePanel.add(new HTML(note));
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
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
		super.clear();
		uploadNotePanel.clear();
		errorPanel.clear();
	}
	/*
	 * Private Methods
	 */

}
