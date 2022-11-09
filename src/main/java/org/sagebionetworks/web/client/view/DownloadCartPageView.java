package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DownloadCartPageView extends IsWidget {
  /**
   * Renders the view
   */
  public void render();

  public void setPresenter(Presenter presenter);

  public interface Presenter {
    void onViewSharingSettingsClicked(String benefactorEntityId);
  }
}
