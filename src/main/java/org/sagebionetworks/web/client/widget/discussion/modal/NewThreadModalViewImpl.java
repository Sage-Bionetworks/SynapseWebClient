package org.sagebionetworks.web.client.widget.discussion.modal;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NewThreadModalViewImpl implements NewThreadModalView {

	public interface Binder extends UiBinder<Widget, NewThreadModalViewImpl> {}

	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Modal newThreadModal;

	private Widget widget;
	private Presenter presenter;

	@Inject
	public NewThreadModalViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newThreadModal.hide();
				presenter.onSave();
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
