package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.core.client.Scheduler;
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
	SimplePanel fileInputWidgetContainer;
	@UiField
	SimplePanel wikiAttachmentsContainer;
	@UiField
	FlowPanel uploadSuccessUI;
	@UiField
	FlowPanel uploadFailureUI;
	@UiField
	Text uploadErrorText;
	@UiField
	Text fileNameText;
	
	private Widget widget;
	
	@Inject
	public AttachmentConfigViewImpl(AttachmentConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void initView() {
		uploadSuccessUI.setVisible(false);
		uploadFailureUI.setVisible(false);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, DialogCallback dialogCallback) {
		fileInputWidgetContainer.setVisible(true);
	}

	@Override
	public void showUploadFailureUI(String error) {
		uploadErrorText.setText(error);
		uploadFailureUI.setVisible(true);
		uploadSuccessUI.setVisible(false);
	}
	
	@Override
	public void showUploadSuccessUI(String fileName) {
		fileInputWidgetContainer.setVisible(false);
		uploadFailureUI.setVisible(false);
		fileNameText.setText(fileName);
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
	public void setWikiAttachmentsWidgetVisible(boolean visible) {
		wikiAttachmentsContainer.setVisible(visible);
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void clear() {
	}
	
	/*
	 * Private Methods
	 */

}
