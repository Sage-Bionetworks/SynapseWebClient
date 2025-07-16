package org.sagebionetworks.web.client.widget;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.EntityCitationProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class EntityCitationImpl extends ReactComponent {

  @Inject
  public EntityCitationImpl() {}

  public void configure(String projectId, String entityId, Double version) {
    EntityCitationProps props = EntityCitationProps.create(
      projectId,
      entityId,
      version
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityCitation,
      props
    );
    this.render(component);
  }
}
