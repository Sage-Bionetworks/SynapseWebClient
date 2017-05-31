package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
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
	Button reloadButton;
	
	@UiField
	Strong alertText;
	@UiField
	Alert alert;
	@UiField
	Div loginAlert;
	Presenter presenter;
	@UiField
	Div loginWidgetContainer;
	
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
		reloadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				reload();
			}
		});
	}
	
	@Override
	public void setRetryButtonVisible(boolean visible) {
		reloadButton.setVisible(visible);
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
		reloadButton.setVisible(false);
	}
	
	@Override
	public void showLogin() {
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
	public void showJiraIssueOpen(String key, String url) {
		Bootbox.alert("The new report <a target=\"_blank\" href=\"" + url + "\">"+key+"</a> has been sent. Thank you for submitting!");
	}
	
	@Override
	public void setLoginWidget(Widget w) {
		loginWidgetContainer.clear();
		loginWidgetContainer.add(w);
	}
	
	@Override
	public void reload() {
		Window.Location.reload();
	}
}
