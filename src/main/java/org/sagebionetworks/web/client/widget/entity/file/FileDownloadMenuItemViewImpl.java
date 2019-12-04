package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.sagebionetworks.web.client.PortalGinInjector;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileDownloadMenuItemViewImpl implements FileDownloadMenuItemView {

	private Presenter presenter;

	@UiField
	AnchorListItem downloadLink;
	PortalGinInjector ginInjector;

	interface FileDownloadMenuItemViewImplUiBinder extends UiBinder<Widget, FileDownloadMenuItemViewImpl> {
	}

	private static FileDownloadMenuItemViewImplUiBinder uiBinder = GWT.create(FileDownloadMenuItemViewImplUiBinder.class);
	Widget widget;
	ClickHandler licensedDownloadClickHandler, authorizedDirectDownloadClickHandler, directDownloadClickHandler;
	HandlerRegistration downloadLinkHandlerRegistration;

	@Inject
	public FileDownloadMenuItemViewImpl(PortalGinInjector ginInjector) {
		widget = uiBinder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		licensedDownloadClickHandler = event -> {
			// if there is an href, ignore it
			event.preventDefault();
			presenter.onUnauthenticatedS3DirectDownloadClicked();
		};

		authorizedDirectDownloadClickHandler = event -> {
			event.preventDefault();
			presenter.onAuthorizedDirectDownloadClicked();
		};
		directDownloadClickHandler = event -> {
			presenter.onDirectDownloadClicked();
		};
	}

	@Override
	public void clear() {}

	private void clearClickHandlers() {
		if (downloadLinkHandlerRegistration != null) {
			downloadLinkHandlerRegistration.removeHandler();
		}
		downloadLink.setHref("#");
	}

	@Override
	public void setIsAuthorizedDirectDownloadLink() {
		clearClickHandlers();
		downloadLinkHandlerRegistration = downloadLink.addClickHandler(authorizedDirectDownloadClickHandler);
	}

	@Override
	public void setIsUnauthenticatedS3DirectDownload() {
		clearClickHandlers();
		downloadLinkHandlerRegistration = downloadLink.addClickHandler(licensedDownloadClickHandler);
	}

	@Override
	public void setIsDirectDownloadLink(String href) {
		clearClickHandlers();
		downloadLink.setHref(href);
		downloadLinkHandlerRegistration = downloadLink.addClickHandler(directDownloadClickHandler);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showLoginS3DirectDownloadDialog(String endpoint) {
		S3DirectLoginDialog dialog = ginInjector.getS3DirectLoginDialog();
		dialog.setPresenter(presenter);
		dialog.showLoginS3DirectDownloadDialog(endpoint);
	}

	@Override
	public void showS3DirectDownloadDialog() {
		S3DirectLoginDialog dialog = ginInjector.getS3DirectLoginDialog();
		dialog.setPresenter(presenter);
		dialog.showS3DirectDownloadDialog();
	}
}
