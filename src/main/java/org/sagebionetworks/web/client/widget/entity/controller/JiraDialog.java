package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JiraDialog {
	
	public interface Binder extends UiBinder<Widget, JiraDialog> {}

	Widget widget;
	
	@UiField
	Modal jiraDialog;
	@UiField
	TextArea userReportField;
	@UiField
	Button okButton;
	@UiField
	Button cancelButton;
	
	@Inject
	public JiraDialog(){
		Binder binder = GWT.create(Binder.class);
		widget = binder.createAndBindUi(this);
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				jiraDialog.hide();
			}
		});
	}
	
	public void addClickHandler(ClickHandler clickHandler) {
		okButton.addClickHandler(clickHandler);
	}
	
	public Widget asWidget() {
		return widget;
	}
	
	public void hideJiraDialog() {
		jiraDialog.hide();
	}
	
	public void showJiraDialog(String errorMessage) {
		widget.setVisible(true);
		jiraDialog.show();
	}
	
	public void reload() {
		Window.Location.reload();
	}
	
	public String getText() {
		return userReportField.getText();
	}
}
