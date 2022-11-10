package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.presenter.DataAccessManagementPresenter;

public interface DataAccessManagementView extends IsWidget {
  /**
   * Set this view's presenter
   *
   * @param presenter
   */
  public void setPresenter(DataAccessManagementPresenter presenter);

  public void render();
}
