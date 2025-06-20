package org.sagebionetworks.web.client.widget;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EntityCitationProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class EntityCitationImpl extends ReactComponent {

  private final SynapseReactClientFullContextPropsProvider contextPropsProvider;

  @Inject
  public EntityCitationImpl(
    SynapseReactClientFullContextPropsProvider contextPropsProvider
  ) {
    this.contextPropsProvider = contextPropsProvider;
  }

  public void configure(String projectId, String entityId, Double version) {
    EntityCitationProps props = EntityCitationProps.create(
      projectId,
      entityId,
      version
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityCitation,
      props,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
