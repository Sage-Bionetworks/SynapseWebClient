package org.sagebionetworks.web.client.widget.entity.act;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class RevokeUserAccessModalViewImpl implements RevokeUserAccessModalView {
	
	public interface Binder extends UiBinder<Widget, RevokeUserAccessModalViewImpl> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	
	@UiField
	Modal modal;
	@UiField
	Div synAlertContainer;
	@UiField
	Button revokeButton;
	@UiField
	Button cancelButton;
	@UiField
	Div userSelectContainer;
	
	private Presenter presenter;
	
	Widget widget;

	public RevokeUserAccessModalViewImpl() {
		widget = uiBinder.createAndBindUi(this);
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		revokeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRevoke();
			}
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void setSynAlert(Widget widget) {
		synAlertContainer.clear();
		synAlertContainer.add(widget.asWidget());		
	}
	
	@Override
	public void setRevokeProcessing(boolean processing) {
		if(processing){
			revokeButton.state().loading();
		}else{
			revokeButton.state().reset();
		}
		cancelButton.setEnabled(!processing);
	}
}
