package org.sagebionetworks.web.client.widget.entity;

import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class SearchV2Impl extends ReactComponent {

  private Header headerWidget;

  @Inject
  public SearchV2Impl(Header headerWidget) {
    this.headerWidget = headerWidget;
  }

  public void configure() {
    headerWidget.configure();
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SynapseSearchPage
    );
    this.render(component);
  }
}
