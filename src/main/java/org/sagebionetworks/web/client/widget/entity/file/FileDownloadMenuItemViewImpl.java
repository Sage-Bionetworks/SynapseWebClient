package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileDownloadMenuItemViewImpl implements FileDownloadMenuItemView {

	private Presenter presenter;
	
	@UiField
	AnchorListItem downloadLink;
	@UiField
	AnchorListItem downloadLink2;
	@UiField
	Span otherWidgets;
	
	PortalGinInjector ginInjector;
	
	boolean isExtraSmall;
	interface FileDownloadMenuItemViewImplUiBinder extends UiBinder<Widget, FileDownloadMenuItemViewImpl> {}

	private static FileDownloadMenuItemViewImplUiBinder uiBinder = GWT.create(FileDownloadMenuItemViewImplUiBinder.class);
	Widget widget;
	ClickHandler licensedDownloadClickHandler, authorizedDirectDownloadClickHandler;
	HandlerRegistration downloadLinkHandlerRegistration, downloadLink2HandlerRegistration;
	
	@Inject
	public FileDownloadMenuItemViewImpl(PortalGinInjector ginInjector) {
		widget = uiBinder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		licensedDownloadClickHandler = event -> {
			//if there is an href, ignore it
			event.preventDefault();
			presenter.onUnauthenticatedS3DirectDownloadClicked();
		};
		
		authorizedDirectDownloadClickHandler = event -> {
			presenter.onAuthorizedDirectDownloadClicked();
		};
	
		isExtraSmall = false;
	}
	
	@Override
	public void clear() {
		downloadLink.setVisible(false);
		downloadLink2.setVisible(false);
		otherWidgets.clear();
	}
	
	private void clearClickHandlers() {
		if (downloadLinkHandlerRegistration != null) {
			downloadLinkHandlerRegistration.removeHandler();
		}
		if (downloadLink2HandlerRegistration != null) {
			downloadLink2HandlerRegistration.removeHandler();
		}
		downloadLink.setHref("#");
		downloadLink2.setHref("#");
	}
	
	@Override
	public void setIsAuthorizedDirectDownloadLink() {
		clearClickHandlers();
		downloadLinkHandlerRegistration = downloadLink.addClickHandler(authorizedDirectDownloadClickHandler);
		downloadLink2HandlerRegistration = downloadLink2.addClickHandler(authorizedDirectDownloadClickHandler);
		downloadLink.setVisible(!isExtraSmall);
		downloadLink2.setVisible(isExtraSmall);
	}
	@Override
	public void setIsUnauthenticatedS3DirectDownload() {
		clearClickHandlers();
		downloadLinkHandlerRegistration = downloadLink.addClickHandler(licensedDownloadClickHandler);
		downloadLink2HandlerRegistration = downloadLink2.addClickHandler(licensedDownloadClickHandler);
		updateDownloadLinkVisibility();
	}
	
	@Override
	public void setIsDirectDownloadLink(String href) {
		clearClickHandlers();
		downloadLink.setHref(href);
		downloadLink2.setHref(href);
		updateDownloadLinkVisibility();
	}
	
	private void updateDownloadLinkVisibility() {
		downloadLink.setVisible(!isExtraSmall);
		downloadLink2.setVisible(isExtraSmall);
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
	public void addWidget(IsWidget w) {
		otherWidgets.add(w);
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
