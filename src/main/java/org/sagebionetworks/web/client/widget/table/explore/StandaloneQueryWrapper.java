package org.sagebionetworks.web.client.widget.table.explore;

import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.StandaloneQueryWrapperProps;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class StandaloneQueryWrapper extends ReactComponentDiv {

  public StandaloneQueryWrapper(
    SynapseReactClientFullContextPropsProvider contextPropsProvider,
    String sql
  ) {
    StandaloneQueryWrapperProps props = StandaloneQueryWrapperProps.create(sql);
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.StandaloneQueryWrapper,
      props,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
