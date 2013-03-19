package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface ShinySiteWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(String siteUrl, int width, int height);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

	public void showInvalidSiteUrl(String siteUrl);
}
