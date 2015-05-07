package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.events.EntityDeletedEvent;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.widget.table.TableRowHeader;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityPageTopView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setEntityBundle(EntityBundle bundle, UserProfile userProfile,
			String entityTypeDisplay, 
			Long versionNumber, Synapse.EntityArea area, String areaToken,
			EntityHeader projectHeader,
			String wikiPageId);

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
		 * @param currentArea 
		 */
		void gotoProjectArea(Synapse.EntityArea area, EntityArea currentArea);
		
		/**
		 * Changes the current active area
		 * @param area
		 */
		void setArea(EntityArea area, String areaToken);

		void replaceArea(EntityArea area, String areaToken);

		void fireEntityUpdatedEvent();

		boolean isLoggedIn();

		String createEntityLink(String id, String version, String display);

		boolean isPlaceChangeForArea(EntityArea targetTab);

		void entityDeleted(EntityDeletedEvent event);

		void setTableQuery(Query newQuery);

		void setTableRow(TableRowHeader rowHeader);
		
		TableRowHeader getTableRowHeader();

		Query getTableQuery();

		void setWikiSubTokenToProjectArea(String wikiPageId);

	}

}
