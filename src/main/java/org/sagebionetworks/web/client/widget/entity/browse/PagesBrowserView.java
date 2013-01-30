package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

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
	public void configure(String ownerId, String ownerType, List<WikiHeader> wikiHeaders, boolean canEdit);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void createPage(String name);

	}
}
