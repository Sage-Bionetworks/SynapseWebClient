package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface EmptyWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
