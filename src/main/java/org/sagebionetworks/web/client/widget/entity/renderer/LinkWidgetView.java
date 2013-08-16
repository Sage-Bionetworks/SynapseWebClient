package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface LinkWidgetView extends IsWidget {
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(String linkText, String linkUrl);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
