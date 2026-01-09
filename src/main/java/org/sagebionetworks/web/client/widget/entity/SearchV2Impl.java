package org.sagebionetworks.web.client.widget.entity;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class SearchV2Impl extends ReactComponent {

  @Inject
  public SearchV2Impl() {}

  public void configure() {
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SynapseSearchPage
    );
    this.render(component);
  }
}
