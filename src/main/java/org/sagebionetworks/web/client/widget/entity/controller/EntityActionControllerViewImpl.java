package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Pre;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.download.AwsLoginView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerViewImpl implements
		EntityActionControllerView {

	public interface Binder extends
			UiBinder<Widget, EntityActionControllerViewImpl> {
	}

	@UiField
	Modal infoDialog;
	@UiField
	Pre infoDialogText;
	@UiField
	Div extraWidgetsContainer;
	@UiField
	Modal awsLoginDialog;
	@UiField
	Div awsLoginContainer;
	@UiField
	Button awsLoginOkButton;
	
	Widget widget;
	Presenter presenter;
	AwsLoginView awsLoginView;
	@Inject
	public EntityActionControllerViewImpl(Binder binder, AwsLoginView awsLoginView){
		widget = binder.createAndBindUi(this);
		this.awsLoginView = awsLoginView;
		awsLoginContainer.add(awsLoginView);
		awsLoginOkButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAwsLogin();
			}
		});
	}

	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showConfirmDialog(String title, String string, Callback callback) {
		DisplayUtils.showConfirmDialog(title, string, callback);
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
	public void showPromptDialog(String prompt, PromptCallback callback) {
		Bootbox.prompt(prompt, callback);
	}
	
	@Override
	public void showInfoDialog(String header, String message) {
		infoDialog.setTitle(header);
		infoDialogText.setText(message);
		infoDialog.show();
	}

	@Override
	public void addWidget(IsWidget w) {
		extraWidgetsContainer.add(w);
	}
	@Override
	public void showAwsLoginDialog() {
		awsLoginDialog.show();
	}
	@Override
	public String getS3DirectAccessKey() {
		return awsLoginView.getAccessKey();
	}
	
	@Override
	public String getS3DirectSecretKey() {
		return awsLoginView.getSecretKey();
	}
}
