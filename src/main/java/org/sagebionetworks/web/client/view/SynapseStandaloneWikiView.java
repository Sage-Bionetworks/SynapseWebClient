package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

public interface SynapseStandaloneWikiView extends IsWidget {
  void configure(String markdown, WikiPageKey wikiKey);

  void setSynAlert(IsWidget w);
}
