package org.sagebionetworks.web.client.widget.trash;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class TrashCanList extends ReactComponentDiv {

  public TrashCanList(SynapseContextPropsProvider contextPropsProvider) {
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.TrashCanList,
      null,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
