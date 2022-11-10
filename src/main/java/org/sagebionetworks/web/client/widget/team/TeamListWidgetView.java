package org.sagebionetworks.web.client.widget.team;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseView;

public interface TeamListWidgetView extends IsWidget, SynapseView {
  public void showEmpty();

  public void addTeam(Team team);

  void showLoading();

  void setNotificationValue(String teamId, Long notificationCount);
}
