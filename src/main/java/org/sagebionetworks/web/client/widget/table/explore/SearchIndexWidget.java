package org.sagebionetworks.web.client.widget.table.explore;

import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SearchQueryWrapperPlotNavProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class SearchIndexWidget extends ReactComponent {

  public SearchIndexWidget(String searchIndexId, String name) {
    SearchQueryWrapperPlotNavProps props =
      SearchQueryWrapperPlotNavProps.create(searchIndexId, name);
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SearchQueryWrapperPlotNav,
      props
    );
    this.render(component);
  }
}
