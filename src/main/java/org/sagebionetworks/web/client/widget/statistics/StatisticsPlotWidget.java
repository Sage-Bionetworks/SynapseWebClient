package org.sagebionetworks.web.client.widget.statistics;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.security.AuthenticationController;

public class StatisticsPlotWidget {

  private StatisticsPlotWidgetView view;
  private AuthenticationController authController;

  @Inject
  public StatisticsPlotWidget(
    StatisticsPlotWidgetView view,
    AuthenticationController authController
  ) {
    this.view = view;
    this.authController = authController;
  }

  public Widget asWidget() {
    view.setVisible(!authController.isLoggedIn());
    return view.asWidget();
  }

  public void configureAndShow(String projectId) {
    if (authController.isLoggedIn()) {
      view.configureAndShow(
        projectId,
        authController.getCurrentUserAccessToken()
      );
    }
  }

  public void clear() {
    view.clear();
  }
}
