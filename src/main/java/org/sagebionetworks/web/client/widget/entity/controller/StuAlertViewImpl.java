package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StuAlertViewImpl implements
		StuAlertView {
	
	public interface Binder extends
			UiBinder<Widget, StuAlertViewImpl> {
	}

	Widget widget;
	
	@UiField
	HTMLPanel httpCode403;
	@UiField
	HTMLPanel httpCode404;
	@UiField
	HTMLPanel synapseDown;
	@UiField
	Div requestAccessUI;
	@UiField
	Anchor requestAccessLink;
	@UiField
	Div requestLoadingUI;
	@UiField
	Div synAlertContainer;
	Presenter presenter;
	@UiField
	Column readOnlyMessage;
	@UiField
	Column synapseDownMessage;
	
	@Inject
	public StuAlertViewImpl(){
		Binder b = GWT.create(Binder.class);
		widget = b.createAndBindUi(this);
		requestAccessLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRequestAccess();
			}
		});
	}
	
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showInfo(String tile, String message) {
		DisplayUtils.showInfo(tile, message);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void clearState() {
		widget.setVisible(false);
		httpCode403.setVisible(false);
		httpCode404.setVisible(false);
		requestAccessUI.setVisible(false);
		requestLoadingUI.setVisible(false);
		synapseDown.setVisible(false);
	}
	
	
	@Override
	public void show403() {
		widget.setVisible(true);
		httpCode403.setVisible(true);
	}
	
	@Override
	public void show404() {
		widget.setVisible(true);
		httpCode404.setVisible(true);
	}
	@Override
	public void showSynapseDown() {
		readOnlyMessage.setVisible(false);
		synapseDownMessage.setVisible(true);
		synapseDown.setVisible(true);
		widget.setVisible(true);
	}
	
	@Override
	public void showReadOnly() {
		readOnlyMessage.setVisible(true);
		synapseDownMessage.setVisible(false);
		synapseDown.setVisible(true);
		widget.setVisible(true);
	}
	
	@Override
	public void showRequestAccessUI() {
		requestLoadingUI.setVisible(false);
		requestAccessUI.setVisible(true);
	}
	@Override
	public void hideRequestAccessUI() {
		requestAccessUI.setVisible(false);
	}
	@Override
	public void showRequestAccessButtonLoading() {
		requestLoadingUI.setVisible(true);
	}
	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
}
