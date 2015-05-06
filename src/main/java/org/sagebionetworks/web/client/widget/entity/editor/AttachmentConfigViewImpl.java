package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentConfigViewImpl implements AttachmentConfigView {
	public interface AttachmentConfigViewImplUiBinder extends UiBinder<Widget, AttachmentConfigViewImpl> {}
	private Presenter presenter;
	
	@UiField
	Button uploadButton;
	@UiField
	SimplePanel fileInputWidgetContainer;
	@UiField
	SimplePanel wikiAttachmentsContainer;
	@UiField
	TabListItem uploadTabListItem;
	@UiField
	TabListItem existingAttachmentListItem;
	@UiField
	FlowPanel uploadSuccessUI;
	@UiField
	FlowPanel uploadFailureUI;
	@UiField
	Text uploadErrorText;
	
	private Widget widget;
	
	@Inject
	public AttachmentConfigViewImpl(AttachmentConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		
		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.uploadFileClicked();
			}
		});
	}
	
	@Override
	public void initView() {
		uploadButton.setEnabled(true);
		uploadSuccessUI.setVisible(false);
		uploadFailureUI.setVisible(false);
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				uploadTabListItem.showTab();
			}
		});
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, DialogCallback dialogCallback) {
		fileInputWidgetContainer.setVisible(true);
		uploadButton.setVisible(true);
	}
	
	@Override
	public void setUploadButtonEnabled(boolean enabled) {
		uploadButton.setEnabled(enabled);
	}
	@Override
	public void showUploadFailureUI(String error) {
		uploadErrorText.setText(error);
		uploadFailureUI.setVisible(true);
		uploadSuccessUI.setVisible(false);
	}
	
	@Override
	public void showUploadSuccessUI() {
		fileInputWidgetContainer.setVisible(false);
		uploadButton.setVisible(false);

		uploadFailureUI.setVisible(false);
		uploadSuccessUI.setVisible(true);
	}
	
	@Override
	public void setFileInputWidget(Widget fileInputWidget) {
		fileInputWidgetContainer.clear();
		fileInputWidgetContainer.setWidget(fileInputWidget);
	}
	
	@Override
	public void setWikiAttachmentsWidget(Widget wikiAttachmentWidget) {
		wikiAttachmentsContainer.clear();
		wikiAttachmentsContainer.setWidget(wikiAttachmentWidget);
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
	}
	
	@Override
	public Widget asWidget() {
		return widget;
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
	public boolean isNewAttachment() {
		return uploadTabListItem.isActive();
	}
	
	@Override
	public boolean isFromAttachments() {
		return existingAttachmentListItem.isActive();
	}
	
	/*
	 * Private Methods
	 */

}
