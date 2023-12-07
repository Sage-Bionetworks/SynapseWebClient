package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

public class TeamSubjectWidget
  implements TeamSubjectWidgetView.Presenter, IsWidget {

  TeamSubjectWidgetView view;
  PortalGinInjector ginInjector;
  RestrictableObjectDescriptor rod;
  CallbackP<TeamSubjectWidget> deletedCallback;

  @Inject
  public TeamSubjectWidget(
    TeamSubjectWidgetView view,
    PortalGinInjector ginInjector
  ) {
    this.view = view;
    this.ginInjector = ginInjector;
    view.setPresenter(this);
  }

  public void configure(
    RestrictableObjectDescriptor rod,
    CallbackP<TeamSubjectWidget> deletedCallback
  ) {
    this.rod = rod;
    this.deletedCallback = deletedCallback;
    if (rod.getType().equals(RestrictableObjectType.TEAM)) {
      TeamBadge teamBadge = ginInjector.getTeamBadgeWidget();
      teamBadge.configure(rod.getId());
      teamBadge.addStyleName("margin-right-5");
      view.setSubjectRendererWidget(teamBadge.asWidget());
    }
    view.setDeleteVisible(deletedCallback != null);
  }

  @Override
  public void onDelete() {
    deletedCallback.invoke(this);
  }

  public RestrictableObjectDescriptor getRestrictableObjectDescriptor() {
    return rod;
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
