package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.web.client.jsinterop.DiscussionThreadProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class DiscussionThreadViewImpl extends ReactComponent {

  public DiscussionThreadViewImpl(String threadId, int limit) {
    DiscussionThreadProps props = DiscussionThreadProps.create(threadId, limit);
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.DiscussionThread,
      props
    );
    this.render(component);
  }
}
