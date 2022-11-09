package org.sagebionetworks.web.client.widget.team.controller;

import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.utils.Callback;

public interface TeamLeaveModalWidgetView {
  Widget asWidget();

  public interface Presenter {
    void onConfirm();

    Widget asWidget();

    void setRefreshCallback(Callback refreshCallback);

    void configure(Team team);
  }

  void setSynAlertWidget(Widget asWidget);

  void setPresenter(Presenter presenter);

  void show();

  void showInfo(String message);

  void hide();
}
