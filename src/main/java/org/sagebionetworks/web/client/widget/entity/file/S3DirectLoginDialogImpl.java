package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.entity.download.AwsLoginView;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class S3DirectLoginDialogImpl implements S3DirectLoginDialog {
	public interface Binder extends UiBinder<Widget, S3DirectLoginDialogImpl> {
	}

	Widget w;
	AwsLoginView awsLoginView;
	@UiField
	Modal s3DirectLoginDialog;
	@UiField
	Div s3DirectLoginDialogBody;
	@UiField
	Button s3DirectLoginDialogButton;
	@UiField
	Modal s3DirectDownloadDialog;
	@UiField
	Button s3DirectDownloadButton;

	Presenter presenter;

	@Inject
	public S3DirectLoginDialogImpl(Binder uiBinder, AwsLoginView awsLoginView) {
		w = uiBinder.createAndBindUi(this);
		this.awsLoginView = awsLoginView;
		s3DirectLoginDialogBody.add(awsLoginView);

		s3DirectLoginDialogButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				s3DirectLoginDialog.hide();
				presenter.onLoginS3DirectDownloadClicked(awsLoginView.getAccessKey(), awsLoginView.getSecretKey());
			}
		});
		s3DirectDownloadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				s3DirectDownloadDialog.hide();
				presenter.onAuthenticatedS3DirectDownloadClicked();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showLoginS3DirectDownloadDialog(String endpoint) {
		awsLoginView.clear();
		awsLoginView.setEndpoint(SafeHtmlUtils.htmlEscape(endpoint));
		s3DirectLoginDialog.show();
	}

	@Override
	public void showS3DirectDownloadDialog() {
		s3DirectDownloadDialog.show();
	}
}
