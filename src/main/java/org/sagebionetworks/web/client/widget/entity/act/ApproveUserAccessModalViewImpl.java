package org.sagebionetworks.web.client.widget.entity.act;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class ApproveUserAccessModalViewImpl implements ApproveUserAccessModalView {
	
	public interface Binder extends UiBinder<Widget, ApproveUserAccessModalViewImpl> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	
	@UiField
	Modal modal;
	@UiField
	Button accessReqNum;
	@UiField
	DropDownMenu arDropdownMenu;
	@UiField
	TextBox accessReqText;
	@UiField
	Button submitButton;
	@UiField
	Button cancelButton;
	@UiField
	Div userSelectContainer;
	
	private Presenter presenter;
	
	Widget widget;

	public ApproveUserAccessModalViewImpl() {
		widget = uiBinder.createAndBindUi(this);
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSubmit();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		//any additional setup should go here
	}
	
	@Override
	public void setStates(List<String> states) {
		arDropdownMenu.clear();
		for (final String state : states) {
			AnchorListItem item = new AnchorListItem(state);
			item.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.onStateSelected(state);
				}
			});
			arDropdownMenu.add(item);
		}
	}
	
	@Override
	public void setUserPickerWidget(Widget w) {
		userSelectContainer.clear();
		userSelectContainer.add(w);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void show() {
		modal.show();
	}
	
	@Override
	public void hide() {
		modal.hide();
	}

	@Override
	public void setAccessRequirement(String num, String text) {
		accessReqNum.setText(num);
		accessReqText.setText(text);
	}
}
