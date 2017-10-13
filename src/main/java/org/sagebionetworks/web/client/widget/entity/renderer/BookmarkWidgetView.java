package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface BookmarkWidgetView extends IsWidget {
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(String bookmarkID, String bookmarkLinkText);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
