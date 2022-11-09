package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface UserListRowWidgetView extends IsWidget {
  void setUserBadge(IsWidget w);

  void setInstitution(String institution);

  void setEmail(String email);
}
