package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface PagesBrowserView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Configure the view with the parent id and title
	 * @param entityId
	 * @param title
	 */
	public void configure(String entityId, String title);

	public void refreshTreeView(String entityId);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void createPage(String name);

		void fireEntityUpdatedEvent();
	}


}
