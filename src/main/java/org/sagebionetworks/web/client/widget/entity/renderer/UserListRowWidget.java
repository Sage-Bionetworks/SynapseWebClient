package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class UserListRowWidget implements IsWidget {

  public static final String SYNAPSE_ORG = "@synapse.org";
  private UserListRowWidgetView view;
  private UserBadge userBadge;

  @Inject
  public UserListRowWidget(UserListRowWidgetView view, UserBadge userBadge) {
    this.view = view;
    this.userBadge = userBadge;
    view.setUserBadge(userBadge);
  }

  public void configure(UserProfile profile) {
    userBadge.configure(profile);
    view.setInstitution(profile.getCompany());
    view.setEmail(profile.getUserName() + SYNAPSE_ORG);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
