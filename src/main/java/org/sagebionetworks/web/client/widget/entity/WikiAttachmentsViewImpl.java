package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiAttachmentsViewImpl implements WikiAttachmentsView {

	private static final String FILENAMES = "filenames";

	interface WikiAttachmentsViewImplUiBinder extends UiBinder<Widget, WikiAttachmentsViewImpl> {
	}

	private static WikiAttachmentsViewImplUiBinder uiBinder = GWT.create(WikiAttachmentsViewImplUiBinder.class);

	private Presenter presenter;
	private Widget widget;

	@UiField
	FlowPanel attachmentsPanel;
	@UiField
	Heading noAttachmentsUI;
	@UiField
	Alert alert;
	List<InlineRadio> radioButtons = new ArrayList<InlineRadio>();

	@Inject
	public WikiAttachmentsViewImpl() {
		widget = uiBinder.createAndBindUi(this);
	}

	public void reset() {
		attachmentsPanel.clear();
		alert.setVisible(false);
		noAttachmentsUI.setVisible(false);
	}

	@Override
	public void showNoAttachmentRow() {
		noAttachmentsUI.setVisible(true);
	}

	@Override
	public void addFileHandles(List<FileHandle> attachments) {
		radioButtons.clear();
		for (int i = 0; i < attachments.size(); i++) {
			FlowPanel row = new FlowPanel();
			attachmentsPanel.add(row);

			FileHandle data = attachments.get(i);
			final String fileName = data.getFileName();
			InlineRadio attachmentLink = new InlineRadio(FILENAMES, data.getFileName());
			radioButtons.add(attachmentLink);
			attachmentLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.setSelectedFilename(fileName);
				}
			});

			if (i == 0) {
				attachmentLink.setValue(true, true);
			}

			row.add(attachmentLink);

			Button button = new Button("", IconType.TIMES, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.deleteAttachment(fileName);
				}
			});
			button.setSize(ButtonSize.EXTRA_SMALL);
			button.addStyleName("displayInline margin-left-5");
			row.add(button);
		}
	}

	@Override
	public void setSelectedFilename(String fileName) {
		for (InlineRadio radio : radioButtons) {
			String text = radio.getText();
			radio.setValue(text.equals(fileName), false);
		}
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
	public void showLoading() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
