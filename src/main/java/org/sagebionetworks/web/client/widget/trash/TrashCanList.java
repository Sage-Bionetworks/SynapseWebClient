package org.sagebionetworks.web.client.widget.trash;

import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class TrashCanList extends ReactComponent {

  public TrashCanList() {
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.TrashCanList,
      null
    );
    this.render(component);
  }
}
