package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseAlertViewImpl implements
		SynapseAlertView {
	
	public interface Binder extends
			UiBinder<Widget, SynapseAlertViewImpl> {
	}

	Widget widget;
	
	@UiField
	Modal jiraDialog;
	@UiField
	TextArea userReportField;
	@UiField
	Button okButton;
	@UiField
	Button cancelButton;
	
	@UiField
	Strong alertText;
	@UiField
	Alert alert;
	@UiField
	HTMLPanel httpCode403;
	@UiField
	HTMLPanel httpCode404;
	
	@UiField
	Alert loginAlert;
	@UiField
	Button loginButton;
	@UiField
	Div requestAccessUI;
	@UiField
	Button requestAccessButton;
	
	Presenter presenter;
	
	@Inject
	public SynapseAlertViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onCreateJiraIssue(userReportField.getText());
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				jiraDialog.hide();
			}
		});
		loginButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onLoginClicked();
			}
		});
		requestAccessButton.addClickHandler(new ClickHandler() {
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
	public void hideJiraDialog() {
		jiraDialog.hide();
	}
	
	@Override
	public void showJiraDialog(String errorMessage) {
		widget.setVisible(true);
		jiraDialog.show();
	}
	
	@Override
	public void clearState() {
		hideJiraDialog();
		alert.setVisible(false);
		alertText.setText("");
		loginAlert.setVisible(false);
		widget.setVisible(false);
		httpCode403.setVisible(false);
		httpCode404.setVisible(false);
		requestAccessUI.setVisible(false);
	}
	
	@Override
	public void showLoginAlert() {
		widget.setVisible(true);
		loginAlert.setVisible(true);	
	}
	
	@Override
	public void showError(String error) {
		widget.setVisible(true);
		alert.setText(error);
		alert.setVisible(true);
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
	public void showRequestAccessUI() {
		requestAccessButton.state().reset();
		requestAccessUI.setVisible(true);
	}
	@Override
	public void hideRequestAccessUI() {
		requestAccessUI.setVisible(false);
	}
	@Override
	public void showRequestAccessButtonLoading() {
		requestAccessButton.state().loading();	
	}
}
