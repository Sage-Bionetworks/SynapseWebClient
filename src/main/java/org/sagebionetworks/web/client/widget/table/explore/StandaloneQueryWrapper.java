package org.sagebionetworks.web.client.widget.table.explore;

import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.StandaloneQueryWrapperProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class StandaloneQueryWrapper extends ReactComponent {

  public StandaloneQueryWrapper(
    SynapseReactClientFullContextPropsProvider contextPropsProvider,
    String sql
  ) {
    StandaloneQueryWrapperProps props = StandaloneQueryWrapperProps.create(sql);
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.StandaloneQueryWrapper,
      props,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
