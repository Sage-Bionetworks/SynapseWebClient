package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserSelectorViewImpl implements UserSelectorView {
	public interface UserSelectorViewImplUiBinder extends UiBinder<Widget, UserSelectorViewImpl> {
	}

	public Widget w;
	@UiField
	Modal modal;
	@UiField
	Div suggestBoxContainer;
	Presenter presenter;

	@Inject
	public UserSelectorViewImpl(UserSelectorViewImplUiBinder binder) {
		w = binder.createAndBindUi(this);
		modal.addShownHandler(event -> {
			presenter.onModalShown();
		});
		modal.addHiddenHandler(event -> {
			presenter.onModalHidden();
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void hide() {
		modal.hide();
	}

	@Override
	public void setSelectBox(Widget w) {
		suggestBoxContainer.clear();
		suggestBoxContainer.add(w);
	}

	@Override
	public void show() {
		modal.show();
	}

	@Override
	public void addModalShownHandler(ModalShownHandler modalShownHandler) {
		modal.addShownHandler(modalShownHandler);
	}
}
