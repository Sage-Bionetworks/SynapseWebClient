package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	public interface Presenter {

		/**
		 * refreshes the entity from the service and redraws the view
		 */
		void refresh();
		/**
		 * Update the area and add the change to the navigation history.
		 * 
		 * @param area
		 * @param areaToken
		 */
		void updateArea(EntityArea area, String areaToken);
		/**
		 * Replace the current area without adding a change to the navigation history.
		 * Instead the current history token is replaced. The URL in the browser will
		 * be changed to reflect the passed area but the page will not be reloaded.
		 * 
		 * @param area
		 * @param areaToken
		 */
		void replaceArea(EntityArea area, String areaToken);
		void clear();
	}

	/**
	 * Set entity to display
	 * @param versionNumber to highlight
	 * @param entity
	 * @param entityMetadata 
	 */
	public void setEntityBundle(EntityBundle bundle, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea area, String areaToken);

	public void show404();
	
	public void show403();
	
	void setBackgroundImageVisible(boolean isVisible);
	void setBackgroundImageUrl(String url);

	void setSynAlertWidget(Widget synAlert);

	void setFooterWidget(IsWidget footerWidget);

	void setHeaderWidget(IsWidget headerWidget);

	void setEntityPageTopWidget(IsWidget entityPageTopWidget);

	void setOpenTeamInvitesWidget(IsWidget openTeamInvitesWidgetWidget);
	
}
