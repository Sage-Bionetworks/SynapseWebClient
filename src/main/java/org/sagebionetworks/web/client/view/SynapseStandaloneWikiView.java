package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.IsWidget;

public interface SynapseStandaloneWikiView extends IsWidget {
	void configure(String markdown, WikiPageKey wikiKey);

	void setSynAlert(IsWidget w);
}
