package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;

public interface FileDownloadMenuItemView extends IsWidget {
  void setPresenter(Presenter presenter);

  void setActionMenu(EntityActionMenu actionMenu);

  void clear();

  void setIsDirectDownloadLink(String href);

  void setIsSFTPDownload();

  void setIsUnauthenticatedS3DirectDownload();

  void showLoginS3DirectDownloadDialog(String endpoint);

  void showS3DirectDownloadDialog();

  /**
   * Presenter interface
   */
  public interface Presenter extends S3DirectLoginDialog.Presenter {
    void onUnauthenticatedS3DirectDownloadClicked();

    void onDirectDownloadClicked();

    void onSFTPDownloadErrorClicked();
  }
}
