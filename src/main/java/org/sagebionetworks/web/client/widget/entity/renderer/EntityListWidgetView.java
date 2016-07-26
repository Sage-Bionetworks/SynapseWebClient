package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityListWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(boolean showDescription);
	
	public void setEntityGroupRecordDisplay(final int rowIndex,
			final EntityGroupRecordDisplay display, boolean isLoggedIn);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
