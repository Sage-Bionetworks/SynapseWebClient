package org.sagebionetworks.web.client.widget.projectdataavailability;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectDataAvailability implements IsWidget {

  ProjectDataAvailabilityView view;

  @Inject
  public ProjectDataAvailability(ProjectDataAvailabilityView view) {
    this.view = view;
  }

  public void setProjectId(String projectId) {
    view.setProjectId(projectId);
  }

  public Widget asWidget() {
    return view.asWidget();
  }
}
