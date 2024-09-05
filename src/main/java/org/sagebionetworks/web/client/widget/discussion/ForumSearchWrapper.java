package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.ForumSearchProps;
import org.sagebionetworks.web.client.jsinterop.ForumSearchProps.OnSearchResultsVisibleHandler;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class ForumSearchWrapper extends ReactComponent {

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
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ForumSearch,
      props,
      contextPropsProvider.getJsInteropContextProps()
    );
    this.render(component);
  }
}
