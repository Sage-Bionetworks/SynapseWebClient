package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileDownloadButtonViewImpl implements FileDownloadButtonView {

	private Presenter presenter;
	
	@UiField
	Anchor downloadLink;
	@UiField
	Anchor downloadLink2;
	@UiField
	Span otherWidgets;
	@UiField
	ButtonElement downloadButton;

	PortalGinInjector ginInjector;
	
	boolean isExtraSmall;
	interface FileDownloadButtonViewImplUiBinder extends UiBinder<Widget, FileDownloadButtonViewImpl> {}

	private static FileDownloadButtonViewImplUiBinder uiBinder = GWT.create(FileDownloadButtonViewImplUiBinder.class);
	Widget widget;
	ClickHandler licensedDownloadClickHandler, authorizedDirectDownloadClickHandler;
	
	@Inject
	public FileDownloadButtonViewImpl(PortalGinInjector ginInjector) {
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
	
	@Override
	public void setIsAuthorizedDirectDownloadLink() {
		downloadLink.addClickHandler(authorizedDirectDownloadClickHandler);
		downloadLink2.addClickHandler(authorizedDirectDownloadClickHandler);
		downloadLink.setVisible(!isExtraSmall);
		downloadLink2.setVisible(isExtraSmall);
	}
	@Override
	public void setIsUnauthenticatedS3DirectDownload() {
		downloadLink.addClickHandler(licensedDownloadClickHandler);
		downloadLink2.addClickHandler(licensedDownloadClickHandler);
		updateDownloadLinkVisibility();
	}
	
	@Override
	public void setIsDirectDownloadLink(String href) {
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
	
	private void removeButtonSizeStyles(ButtonElement el) {
		for (ButtonSize size : ButtonSize.values()) {
			String cssName = size.getCssName();
			if (DisplayUtils.isDefined(cssName))
				el.removeClassName(cssName);
		}
	}
	
	@Override
	public void setButtonSize(ButtonSize size) {
		isExtraSmall = size.equals(ButtonSize.EXTRA_SMALL);
		removeButtonSizeStyles(downloadButton);
		downloadButton.addClassName(size.getCssName());
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
