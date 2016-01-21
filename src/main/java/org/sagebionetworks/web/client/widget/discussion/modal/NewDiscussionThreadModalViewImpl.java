package org.sagebionetworks.web.client.widget.discussion.modal;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NewDiscussionThreadModalViewImpl implements NewDiscussionThreadModalView {

	public interface Binder extends UiBinder<Widget, NewDiscussionThreadModalViewImpl> {}

	private static final String NEW_THREAD_MODAL_TITLE = "New Thread";

	private static final String SUCCESS_TITLE = "Thread created";

	private static final String SUCCESS_MESSAGE = "A new thread has been created.";

	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Modal newThreadModal;
	@UiField
	TextBox threadTitle;
	@UiField
	TextArea messageMarkdown;
	@UiField
	Div synAlertContainer;

	private Widget widget;
	private Presenter presenter;

	@Inject
	public NewDiscussionThreadModalViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		newThreadModal.setTitle(NEW_THREAD_MODAL_TITLE);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newThreadModal.hide();
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
		newThreadModal.show();
	}

	@Override
	public void hideDialog() {
		newThreadModal.hide();
	}

	@Override
	public String getTitle() {
		return threadTitle.getText();
	}

	@Override
	public String getMessageMarkdown() {
		return messageMarkdown.getText();
	}

	@Override
	public void clear() {
		threadTitle.setText("");
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
