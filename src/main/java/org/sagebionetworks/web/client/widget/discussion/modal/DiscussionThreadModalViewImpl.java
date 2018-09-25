package org.sagebionetworks.web.client.widget.discussion.modal;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadModalViewImpl implements DiscussionThreadModalView {

	public interface Binder extends UiBinder<Widget, DiscussionThreadModalViewImpl> {}

	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Modal threadModal;
	@UiField
	TextBox threadTitle;
	@UiField
	Div markdownEditorContainer;
	@UiField
	Div synAlertContainer;

	private Widget widget;
	private Presenter presenter;

	@Inject
	public DiscussionThreadModalViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		ClickHandler onCancel = event -> {
			presenter.onCancel();
		};
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		cancelButton.addClickHandler(onCancel);
		threadModal.addDomHandler(DisplayUtils.getESCKeyDownHandler(onCancel), KeyDownEvent.getType());
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
		threadModal.show();
		DisplayUtils.focusOnChildInput(threadModal);
	}

	@Override
	public void hideDialog() {
		threadModal.hide();
	}

	@Override
	public String getThreadTitle() {
		return threadTitle.getText();
	}

	@Override
	public void clear() {
		threadTitle.setText("");
		saveButton.state().reset();
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void showSuccess(String title, String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showSaving() {
		saveButton.state().loading();
	}

	@Override
	public void resetButton() {
		saveButton.state().reset();
	}

	@Override
	public void setThreadTitle(String currentTitle) {
		threadTitle.setText(currentTitle);
	}

	@Override
	public void setModalTitle(String title) {
		threadModal.setTitle(title);
	}

	@Override
	public void setMarkdownEditor(Widget widget) {
		markdownEditorContainer.add(widget);
	}

	@Override
	public void showConfirmDialog(String restoreTitle, String restoreMessage, Callback yesCallback,
			Callback noCallback) {
		DisplayUtils.showConfirmDialog(restoreTitle, restoreMessage, yesCallback, noCallback);
	}
}
