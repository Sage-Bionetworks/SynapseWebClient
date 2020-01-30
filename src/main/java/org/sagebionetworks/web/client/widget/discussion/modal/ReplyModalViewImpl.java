package org.sagebionetworks.web.client.widget.discussion.modal;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyModalViewImpl implements ReplyModalView {

	public interface Binder extends UiBinder<Widget, ReplyModalViewImpl> {
	}

	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Modal replyModal;
	@UiField
	Div markdownEditorContainer;
	@UiField
	Div synAlertContainer;

	private Widget widget;
	private Presenter presenter;

	@Inject
	public ReplyModalViewImpl(Binder binder) {
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
		replyModal.addDomHandler(DisplayUtils.getESCKeyDownHandler(onCancel), KeyDownEvent.getType());
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
		replyModal.show();
		DisplayUtils.focusOnChildInput(replyModal);
	}

	@Override
	public void hideDialog() {
		replyModal.hide();
	}

	@Override
	public void setMarkdownEditor(Widget widget) {
		markdownEditorContainer.add(widget);
	}

	@Override
	public void clear() {
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
	public void setModalTitle(String title) {
		replyModal.setTitle(title);
	}
}
