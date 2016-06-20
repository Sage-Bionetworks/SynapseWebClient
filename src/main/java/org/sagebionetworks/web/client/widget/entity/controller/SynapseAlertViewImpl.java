package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
	Alert loginAlert;
	@UiField
	Alert suggestLoginAlert;
	@UiField
	Button loginButton;
	@UiField
	Button loginButton2;
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
		loginButton2.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onLoginClicked();
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
		suggestLoginAlert.setVisible(false);
		widget.setVisible(false);
	}
	
	@Override
	public void showLoginAlert() {
		widget.setVisible(true);
		loginAlert.setVisible(true);	
	}
	
	@Override
	public void showSuggestLoginAlert() {
		widget.setVisible(true);
		suggestLoginAlert.setVisible(true);	
	}
	
	@Override
	public void showError(String error) {
		widget.setVisible(true);
		alert.setText(error);
		alert.setVisible(true);
	}
	
	@Override
	public void showJiraIssueOpen(String key, String url) {
		Bootbox.alert("The new report <a target=\"_blank\" href=\"" + url + "\">"+key+"</a> has been sent. Thank you for submitting!");
	}
}
