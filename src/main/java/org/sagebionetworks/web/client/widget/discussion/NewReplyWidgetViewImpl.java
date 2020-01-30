package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NewReplyWidgetViewImpl implements NewReplyWidgetView {

	public interface Binder extends UiBinder<Widget, NewReplyWidgetViewImpl> {
	}

	@UiField
	TextBox replyTextBox;
	@UiField
	Div newReplyContainer;
	@UiField
	Div markdownEditorContainer;
	@UiField
	Button cancelButton;
	@UiField
	Button saveButton;
	@UiField
	Div synAlert;

	private Presenter presenter;
	private Widget widget;

	@Inject
	public NewReplyWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		replyTextBox.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickNewReply();
			}
		});
		ClickHandler onCancel = event -> {
			presenter.onCancel();
		};
		cancelButton.addClickHandler(onCancel);
		widget.addDomHandler(DisplayUtils.getESCKeyDownHandler(onCancel), KeyDownEvent.getType());
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
	}

	@Override
	public void setReplyTextBoxVisible(boolean visible) {
		replyTextBox.setVisible(visible);
	}

	@Override
	public void resetButton() {
		saveButton.state().reset();
	}

	@Override
	public void setNewReplyContainerVisible(boolean visible) {
		newReplyContainer.setVisible(visible);
	}

	@Override
	public void setMarkdownEditor(Widget widget) {
		markdownEditorContainer.add(widget);
	}

	@Override
	public void showSaving() {
		saveButton.state().loading();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setAlert(Widget widget) {
		this.synAlert.clear();
		synAlert.add(widget);
	}

	@Override
	public void showSuccess(String title, String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public Widget asWidget() {
		return this.widget;
	}

	@Override
	public void showErrorMessage(String error) {
		DisplayUtils.showErrorMessage(error);
	}

	@Override
	public void showConfirmDialog(String restoreTitle, String restoreMessage, Callback yesCallback, Callback noCallback) {
		DisplayUtils.showConfirmDialog(restoreTitle, restoreMessage, yesCallback, noCallback);
	}

	@Override
	public void scrollIntoView() {
		Scheduler.get().scheduleDeferred(() -> {
			Window.scrollTo(0, markdownEditorContainer.getAbsoluteTop());
		});
	}

}
