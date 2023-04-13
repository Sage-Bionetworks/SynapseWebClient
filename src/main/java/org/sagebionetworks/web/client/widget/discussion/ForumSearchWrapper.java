package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.ForumSearchProps;
import org.sagebionetworks.web.client.jsinterop.ForumSearchProps.OnSearchResultsVisibleHandler;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class ForumSearchWrapper extends ReactComponentDiv {

  public ForumSearchWrapper(
    SynapseReactClientFullContextPropsProvider contextPropsProvider,
    String forumId,
    String projectId,
    OnSearchResultsVisibleHandler onSearchResultsVisible
  ) {
    ForumSearchProps props = ForumSearchProps.create(
      forumId,
      projectId,
      onSearchResultsVisible
    );
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ForumSearch,
      props,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
