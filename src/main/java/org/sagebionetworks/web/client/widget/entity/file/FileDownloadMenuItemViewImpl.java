package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.jsinterop.ReactMouseEvent;
import org.sagebionetworks.web.client.jsinterop.ReactMouseEventHandler;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;

public class FileDownloadMenuItemViewImpl implements FileDownloadMenuItemView {

  private Presenter presenter;
  private EntityActionMenu actionMenu;
  String href;

  FlowPanel panel;
  PortalGinInjector ginInjector;
  ReactMouseEventHandler licensedDownloadClickHandler, directDownloadClickHandler, sftpDownloadClickHandler;

  @Inject
  public FileDownloadMenuItemViewImpl(PortalGinInjector ginInjector) {
    this.panel = new FlowPanel();
    this.ginInjector = ginInjector;
    licensedDownloadClickHandler =
      event -> {
        // if there is an href, ignore it
        event.preventDefault();
        presenter.onUnauthenticatedS3DirectDownloadClicked();
      };
    directDownloadClickHandler =
      event -> {
        presenter.onDirectDownloadClicked();
      };
    sftpDownloadClickHandler =
      event -> {
        presenter.onSFTPDownloadErrorClicked();
      };
  }

  public void clear() {}

  private void clearClickHandlers() {
    this.actionMenu.setActionHref(Action.DOWNLOAD_FILE, "#");
  }

  @Override
  public void setIsSFTPDownload() {
    String oldHref = this.href;
    clearClickHandlers();
    this.href = oldHref;
    actionMenu.setActionHref(Action.DOWNLOAD_FILE, this.href);
    actionMenu.addActionListener(
      Action.DOWNLOAD_FILE,
      (Action action, ReactMouseEvent event) ->
        sftpDownloadClickHandler.onClick(event)
    );
  }

  @Override
  public void setIsUnauthenticatedS3DirectDownload() {
    clearClickHandlers();
    actionMenu.setActionListener(
      Action.DOWNLOAD_FILE,
      (Action action, ReactMouseEvent event) ->
        licensedDownloadClickHandler.onClick(event)
    );
  }

  @Override
  public void setIsDirectDownloadLink(String href) {
    clearClickHandlers();
    this.href = href;
    actionMenu.setActionHref(Action.DOWNLOAD_FILE, href);
    actionMenu.addActionListener(
      Action.DOWNLOAD_FILE,
      (Action action, ReactMouseEvent event) ->
        directDownloadClickHandler.onClick(event)
    );
  }

  @Override
  public Widget asWidget() {
    return panel.asWidget();
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setActionMenu(EntityActionMenu actionMenu) {
    this.actionMenu = actionMenu;
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
