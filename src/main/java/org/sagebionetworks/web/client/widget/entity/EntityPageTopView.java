package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityPageTopView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setEntityBundle(EntityBundle bundle, UserProfile userProfile, String entityTypeDisplay, boolean isAdmin, boolean canEdit, Long versionNumber, Synapse.EntityArea area, String areaToken);

	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void refresh();

		/**
		 * Refreshes a given area
		 * @param area
		 * @param areaToken
		 */
		void refreshArea(Synapse.EntityArea area, String areaToken);
		
		/**
		 * Changes places to the specified are of the Parent Project
		 * @param area
		 */
		void gotoProjectArea(Synapse.EntityArea area);
		
		/**
		 * Changes the current active area
		 * @param area
		 */
		void setArea(EntityArea area, String areaToken);

		void fireEntityUpdatedEvent();

		boolean isLoggedIn();

		void loadShortcuts(int offset, int limit, AsyncCallback<PaginatedResults<EntityHeader>> asyncCallback);

		String createEntityLink(String id, String version, String display);

		ImageResource getIconForType(String typeString);

				
	}

}
