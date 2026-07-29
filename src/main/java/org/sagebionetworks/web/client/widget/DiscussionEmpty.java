package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.DiscussionEmptyProps;

public interface DiscussionEmpty extends IsWidget {
  void configure(DiscussionEmptyProps.Callback onViewForumClicked);
}
