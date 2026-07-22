package org.sagebionetworks.web.client.widget;

import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.ProjectInfoProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class ProjectInfoImpl extends ReactComponent implements ProjectInfo {

  @Inject
  public ProjectInfoImpl() {}

  @Override
  public void configure(String projectId) {
    ProjectInfoProps props = ProjectInfoProps.create(projectId);

    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ProjectInfo,
      props
    );
    this.render(component);
  }
}
