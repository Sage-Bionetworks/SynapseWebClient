package org.sagebionetworks.web.client.widget;

import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.ProjectVisibilityChipContainerProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class ProjectVisibilityChipImpl
  extends ReactComponent
  implements ProjectVisibilityChip {

  @Inject
  public ProjectVisibilityChipImpl() {}

  @Override
  public void configure(String entityId) {
    ProjectVisibilityChipContainerProps props =
      ProjectVisibilityChipContainerProps.create(entityId);
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ProjectVisibilityChip,
      props
    );
    this.render(component);
  }
}
