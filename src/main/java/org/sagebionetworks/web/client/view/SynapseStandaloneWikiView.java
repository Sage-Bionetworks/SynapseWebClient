package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface SynapseStandaloneWikiView extends IsWidget, SynapseView {
	void setPresenter(Presenter presenter);
	void configure(String markdown, WikiPageKey wikiKey);
	public interface Presenter extends SynapsePresenter {
	}

}
