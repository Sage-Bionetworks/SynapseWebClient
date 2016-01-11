package org.sagebionetworks.web.client.widget.discussion.modal;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NewThreadModalViewImpl implements NewThreadModalView {

	public interface Binder extends UiBinder<Widget, NewThreadModalViewImpl> {}

	private static final String NEW_THREAD_MODAL_TITLE = "New Thread";

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

	private Widget widget;
	private Presenter presenter;

	@Inject
	public NewThreadModalViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		newThreadModal.setTitle(NEW_THREAD_MODAL_TITLE);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newThreadModal.hide();
				presenter.onSave(threadTitle.getText(), messageMarkdown.getText());
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newThreadModal.hide();
				presenter.onCancel();
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
}
