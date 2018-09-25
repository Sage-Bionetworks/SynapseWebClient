package org.sagebionetworks.web.client.widget.team.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamLeaveModalWidgetViewImpl implements IsWidget, TeamLeaveModalWidgetView {

	@UiField
	Modal modal;
	
	@UiField
	Button primaryButton;
	
	@UiField
	Button cancelButton;
	
	@UiField
	SimplePanel synAlertPanel;
	
	public interface Binder extends UiBinder<Widget, TeamLeaveModalWidgetViewImpl> {}
	
	Widget widget;
	Presenter presenter;
	
	@Inject
	public TeamLeaveModalWidgetViewImpl(Binder uiBinder) {
		this.widget = uiBinder.createAndBindUi(this);
		primaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onConfirm();
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
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void show() {
		this.modal.show();
	}
	
	@Override
	public void hide() {
		this.modal.hide();
	}
	
	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
}
