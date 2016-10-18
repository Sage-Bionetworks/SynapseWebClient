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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class ApproveUserAccessModalViewImpl implements ApproveUserAccessModalView {
	@UiField
	Modal modal;
	@UiField
	TextBox nameField;
	@UiField
	DropDownMenu arDropdownMenu;
	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	Widget widget;
	
	private Presenter presenter;
	
	public interface Binder extends UiBinder<Widget, ApproveUserAccessModalViewImpl> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	
	public ApproveUserAccessModalViewImpl() {
		widget = uiBinder.createAndBindUi(this);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
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
	}
	
	@Override
	public void setStates(List<String> states) {
		arDropdownMenu.clear();
		for (final String state : states) {
			AnchorListItem item = new AnchorListItem(state);
			item.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//presenter.onStateSelected(state);
				}
			});
			arDropdownMenu.add(item);
		}
	}
	
	@Override
	public String getEvaluationName() {
		return nameField.getText();
	}
	@Override
	public void setEvaluationName(String name) {
		nameField.setText(name);
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
}
