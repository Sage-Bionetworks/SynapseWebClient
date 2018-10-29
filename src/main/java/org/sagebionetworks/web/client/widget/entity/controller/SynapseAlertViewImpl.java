package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

public class SynapseAlertViewImpl implements
		SynapseAlertView {
	
	public interface Binder extends
			UiBinder<Widget, SynapseAlertViewImpl> {
	}
	private static Binder uiBinder = GWT.create(Binder.class);

	Widget widget = null;
	
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
	@UiField
	Div jiraDialogContainer;
	ClickHandler onCreateJiraIssue;
	JiraDialog jiraDialog;
	
	Span synapseAlertContainer = new Span();
	public SynapseAlertViewImpl(){
		onCreateJiraIssue = event -> {
			presenter.onCreateJiraIssue(jiraDialog.getText());
		};
	}
	
	private void lazyConstruct() {
		if (widget == null) {
			synapseAlertContainer.setVisible(false);
			widget = uiBinder.createAndBindUi(this);
			synapseAlertContainer.add(widget);
			reloadButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					reload();
				}
			});
			clearState();
		}
	}
	
	@Override
	public void setRetryButtonVisible(boolean visible) {
		lazyConstruct();
		reloadButton.setVisible(visible);
	}
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return synapseAlertContainer;
	}
	
	@Override
	public void hideJiraDialog() {
		if (jiraDialog != null) {
			jiraDialog.hideJiraDialog();	
		}
	}
	
	@Override
	public void showJiraDialog(String errorMessage) {
		lazyConstruct();
		if (jiraDialog == null) {
			jiraDialog = new JiraDialog();
			jiraDialog.addClickHandler(onCreateJiraIssue);
			jiraDialogContainer.add(jiraDialog.asWidget());
		}
		synapseAlertContainer.setVisible(true);
		jiraDialog.showJiraDialog(errorMessage);
	}
	
	@Override
	public void clearState() {
		if (widget != null) {
			hideJiraDialog();
			alert.setVisible(false);
			alertText.setText("");
			loginAlert.setVisible(false);
			reloadButton.setVisible(false);
		}
	}
	
	@Override
	public void showLogin() {
		lazyConstruct();
		synapseAlertContainer.setVisible(true);
		loginAlert.setVisible(true);	
	}
	
	@Override
	public void showError(String error) {
		lazyConstruct();
		synapseAlertContainer.setVisible(true);
		alertText.setText(error);
		alert.setVisible(true);
	}
	
	@Override
	public void showJiraIssueOpen(String key, String url) {
		Bootbox.alert("The new report <a target=\"_blank\" href=\"" + url + "\">"+key+"</a> has been sent. Thank you for submitting!");
	}
	
	@Override
	public void setLoginWidget(Widget w) {
		lazyConstruct();
		loginWidgetContainer.clear();
		loginWidgetContainer.add(w);
	}
	
	@Override
	public void reload() {
		Window.Location.reload();
	}
}
