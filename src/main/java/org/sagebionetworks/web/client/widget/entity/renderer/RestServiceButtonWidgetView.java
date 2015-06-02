package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface RestServiceButtonWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(WikiPageKey wikiKey, String buttonText, String url, boolean isHighlight, boolean openInNewWindow);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
