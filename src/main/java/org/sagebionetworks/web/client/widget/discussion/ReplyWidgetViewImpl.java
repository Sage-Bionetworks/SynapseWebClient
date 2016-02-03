package org.sagebionetworks.web.client.widget.discussion;

import static org.sagebionetworks.web.shared.WebConstants.*;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidgetViewImpl implements ReplyWidgetView {

	private static final String CONFIRM_DELETE_DIALOG_TITLE = "Confirm Deletion";

	public interface Binder extends UiBinder<Widget, ReplyWidgetViewImpl> {}

	@UiField
	SimplePanel author;
	@UiField
	Span createdOn;
	@UiField
	Paragraph replyMessage;
	@UiField
	Div synAlertContainer;
	@UiField
	Button deleteButton;

	private Widget widget;
	private ReplyWidget presenter;

	@Inject
	public ReplyWidgetViewImpl (Binder binder) {
		widget = binder.createAndBindUi(this);
		deleteButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickDeleteReply();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(ReplyWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setAuthor(Widget author){
		this.author.add(author);
	}

	@Override
	public void setCreatedOn(String createdOn) {
		this.createdOn.setText(createdOn);
	}

	@Override
	public void setMessage(String message) {
		this.replyMessage.setText(message);
	}

	@Override
	public void clear() {
		this.createdOn.clear();
		this.replyMessage.clear();
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void setDeleteButtonVisibility(Boolean visible) {
		deleteButton.setVisible(visible);
	}

	@Override
	public void showDeleteConfirm(String deleteConfirmMessage, AlertCallback deleteCallback) {
		Bootbox.Dialog.create()
		.setMessage(deleteConfirmMessage)
		.setCloseButton(false)
		.setTitle(CONFIRM_DELETE_DIALOG_TITLE)
		.addButton(DELETE, DANGER_BUTTON_STYLE, deleteCallback)
		.addButton(CANCEL, PRIMARY_BUTTON_STYLE)
		.show();
	}
}
