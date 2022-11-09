package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.ShowsErrors;

public interface ChallengeBadgeView extends IsWidget, ShowsErrors {
  void setProjectName(String projectName);

  void setProjectId(String projectId);
}
