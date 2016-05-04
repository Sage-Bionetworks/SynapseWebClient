package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserSelectorViewImpl implements UserSelectorView {
	public interface UserSelectorViewImplUiBinder extends UiBinder<Widget, UserSelectorViewImpl> {}
	public Widget w;
	@UiField
	Modal modal;
	@UiField
	Div suggestBoxContainer;
	@UiField
	Div synAlertContainer;
	Presenter presenter;
	@Inject
	public UserSelectorViewImpl(UserSelectorViewImplUiBinder binder) {
		w = binder.createAndBindUi(this);
		modal.addShownHandler(new ModalShownHandler() {
			@Override
			public void onShown(ModalShownEvent evt) {
				presenter.onModalShown();
			}
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
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public void show() {
		modal.show();
	}
}
