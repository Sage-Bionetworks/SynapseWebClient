package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiAttachmentsViewImpl implements WikiAttachmentsView {
	
	interface WikiAttachmentsViewImplUiBinder extends UiBinder<Widget, WikiAttachmentsViewImpl> {}
	
	private static WikiAttachmentsViewImplUiBinder uiBinder = GWT
			.create(WikiAttachmentsViewImplUiBinder.class);

	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;
	private Modal modal;
	
	@UiField
	FlowPanel attachmentsPanel;
	@UiField
	Heading noAttachmentsUI;
	@UiField
	Alert alert;
	
	@Inject
	public WikiAttachmentsViewImpl(SynapseJSNIUtils synapseJsniUtils) {
		this.synapseJsniUtils = synapseJsniUtils;
		modal = (Modal)uiBinder.createAndBindUi(this);
	}
	
	@Override
	public void show() {
		modal.show();
	}
	
	@Override
	public void hide() {
		modal.hide();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, List<FileHandle> list) {		
		reset();
		if(list == null || list.size() == 0){
			showNoAttachmentRow();
		} else {
			populateStore(wikiKey, list);			
		}
	}
	
	public void reset(){
		attachmentsPanel.clear();
		alert.setVisible(false);
		noAttachmentsUI.setVisible(false);
	}
	
	private void showNoAttachmentRow() {
		noAttachmentsUI.setVisible(true);
	}
	
	private void populateStore(WikiPageKey wikiKey, List<FileHandle> attachments) {
		for (int i = 0; i < attachments.size(); i++) {
			FlowPanel row = new FlowPanel();
			attachmentsPanel.add(row);
			
			FileHandle data = attachments.get(i);
			final String fileName = data.getFileName();
			Anchor attachmentLink = new Anchor(data.getFileName());
			attachmentLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.attachmentClicked(fileName);
				}
			});
			
			row.add(attachmentLink);
			
			Button button = new Button("", IconType.TIMES, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.deleteAttachment(fileName);		
				}
			});
			button.setSize(ButtonSize.EXTRA_SMALL);
			button.addStyleName("displayInline margin-left-3");
			row.add(button);
		}
	}


	@Override
	public Widget asWidget() {
		return modal;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
