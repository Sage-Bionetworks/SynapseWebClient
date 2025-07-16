package org.sagebionetworks.web.client.widget.projectdataavailability;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.ProjectDataAvailabilityProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class ProjectDataAvailabilityViewImpl
  implements ProjectDataAvailabilityView {

  ReactComponent container = new ReactComponent();

  @Inject
  public ProjectDataAvailabilityViewImpl() {}

  @Override
  public Widget asWidget() {
    return container;
  }

  @Override
  public void setProjectId(String projectId) {
    ProjectDataAvailabilityProps props = ProjectDataAvailabilityProps.create(
      projectId,
      null
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ProjectDataAvailability,
      props
    );
    container.render(component);
  }
}
