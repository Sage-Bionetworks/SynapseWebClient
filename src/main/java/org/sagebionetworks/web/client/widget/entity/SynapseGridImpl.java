package org.sagebionetworks.web.client.widget.entity;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseGridProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class SynapseGridImpl extends ReactComponent {

  @Inject
  public SynapseGridImpl() {}

  public void configure(String sessionId, Boolean showDebugInfo) {
    SynapseGridProps props = SynapseGridProps.create(
      showDebugInfo,
      node -> node.loadExistingSession(sessionId)
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SynapseGrid,
      props
    );
    this.render(component);
  }
}
