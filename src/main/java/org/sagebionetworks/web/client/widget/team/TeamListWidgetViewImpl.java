package org.sagebionetworks.web.client.widget.team;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.HasNotificationUI;

public class TeamListWidgetViewImpl
  extends FlowPanel
  implements TeamListWidgetView {

  private PortalGinInjector ginInjector;
  private Map<String, HasNotificationUI> team2Badge = new HashMap<
    String,
    HasNotificationUI
  >();
  Widget emptyHTML;
  public static final String EMPTY_DISPLAY =
    "&#8212" + " " + DisplayConstants.EMPTY;

  @Inject
  public TeamListWidgetViewImpl(PortalGinInjector ginInjector) {
    this.ginInjector = ginInjector;
    this.emptyHTML =
      new HTML(
        SafeHtmlUtils
          .fromSafeConstant(
            "<div class=\"smallGreyText\">" + EMPTY_DISPLAY + "</div>"
          )
          .asString()
      );
  }

  @Override
  public void clear() {
    super.clear();
    team2Badge = new HashMap<String, HasNotificationUI>();
  }

  @Override
  public void showEmpty() {
    add(emptyHTML);
  }

  @Override
  public void showLoading() {
    clear();
    add(DisplayUtils.getLoadingWidget());
  }

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void addTeam(Team team) {
    emptyHTML.setVisible(false);
    SimplePanel container = new SimplePanel();
    container.addStyleName("margin-top-10");
    TeamBadge teamRenderer = ginInjector.getTeamBadgeWidget();
    teamRenderer.configure(team);
    team2Badge.put(team.getId(), teamRenderer);
    Widget teamRendererWidget = teamRenderer.asWidget();
    container.setWidget(teamRendererWidget);
    add(container);
  }

  @Override
  public void setNotificationValue(String teamId, Long notificationCount) {
    if (notificationCount != null && notificationCount > 0) {
      team2Badge
        .get(teamId)
        .setNotificationValue(String.valueOf(notificationCount));
    }
  }
}
