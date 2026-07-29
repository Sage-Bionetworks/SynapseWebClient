package org.sagebionetworks.web.client.widget;

import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.DiscussionEmptyProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class DiscussionEmptyImpl
  extends ReactComponent
  implements DiscussionEmpty {

  @Inject
  public DiscussionEmptyImpl() {}

  public void configure(DiscussionEmptyProps.Callback onViewForumClicked) {
    DiscussionEmptyProps props = DiscussionEmptyProps.create(
      onViewForumClicked
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.DiscussionEmpty,
      props
    );
    this.render(component);
  }
}
