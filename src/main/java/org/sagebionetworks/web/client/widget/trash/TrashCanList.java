package org.sagebionetworks.web.client.widget.trash;

import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class TrashCanList extends ReactComponent {

  public TrashCanList(
    SynapseReactClientFullContextPropsProvider contextPropsProvider
  ) {
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.TrashCanList,
      null,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
