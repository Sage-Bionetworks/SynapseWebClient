package org.sagebionetworks.web.client.widget.discussion.modal;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NewReplyModalViewImpl implements NewReplyModalView {

	public interface Binder extends UiBinder<Widget, NewReplyModalViewImpl> {}

	private static final String NEW_REPLY_MODAL_TITLE = "New Reply";

	private static final String SUCCESS_TITLE = "Reply created";

	private static final String SUCCESS_MESSAGE = "A new reply has been created.";

	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Modal newReplyModal;
	@UiField
	TextArea messageMarkdown;
	@UiField
	Div synAlertContainer;

	private Widget widget;
	private Presenter presenter;

	@Inject
	public NewReplyModalViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		newReplyModal.setTitle(NEW_REPLY_MODAL_TITLE);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newReplyModal.hide();
			}
		});
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
	public void showDialog() {
		newReplyModal.show();
	}

	@Override
	public void hideDialog() {
		newReplyModal.hide();
	}

	@Override
	public String getMessageMarkdown() {
		return messageMarkdown.getText();
	}

	@Override
	public void clear() {
		messageMarkdown.setText("");
		saveButton.state().reset();
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void showSuccess() {
		DisplayUtils.showInfo(SUCCESS_TITLE, SUCCESS_MESSAGE);
	}

	@Override
	public void showSaving() {
		saveButton.state().loading();
	}

	@Override
	public void resetButton() {
		saveButton.state().reset();
	}
}
