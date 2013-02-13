package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TreeItem;

public interface PagesBrowserView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Configure the view with the parent id
	 * @param entityId
	 * @param title
	 */
	public void configure(boolean canEdit, TreeItem root);
	
	public String getHTML(String href, String title, boolean isCurrentPage);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void createPage(String name);

	}
}
