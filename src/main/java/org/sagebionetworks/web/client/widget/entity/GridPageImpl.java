package org.sagebionetworks.web.client.widget.entity;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class GridPageImpl extends ReactComponent {

  @Inject
  public GridPageImpl() {}

  public void configure() {
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.GridPage
    );
    this.render(component);
  }
}
