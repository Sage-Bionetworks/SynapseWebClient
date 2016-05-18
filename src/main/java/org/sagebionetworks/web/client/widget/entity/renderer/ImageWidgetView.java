package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface ImageWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(WikiPageKey wikiKey, String fileName, String scale, String alignment, String synapseId, boolean isLoggedIn, Long wikiVersion);
	
	void addStyleName(String style);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
