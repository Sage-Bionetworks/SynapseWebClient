package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface ImageWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(WikiPageKey wikiKey, String fileName, String explicitWidth, String alignment);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
