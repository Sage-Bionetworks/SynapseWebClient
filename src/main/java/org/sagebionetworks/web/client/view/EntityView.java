package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityView extends IsWidget, SynapseView {
		
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
		void updateEntityArea(EntityArea area, String areaToken);
		/**
		 * Replace the current area without adding a change to the navigation history.
		 * Instead the current history token is replaced. The URL in the browser will
		 * be changed to reflect the passed area but the page will not be reloaded.
		 * 
		 * @param area
		 * @param areaToken
		 */
		void replaceEntityArea(EntityArea area, String areaToken);
		void clear();
		void setEntityBundle(EntityBundle bundle, Long versionNumber,
				EntityHeader projectHeader, EntityArea area, String areaToken);
	}
	/**
	 * Set entity to display
	 * @param versionNumber to highlight
	 * @param entity
	 * @param entityMetadata 
	 */
	void setBackgroundImageVisible(boolean isVisible);
	void setBackgroundImageUrl(String url);
	void setFooterWidget(IsWidget footerWidget);
	void setHeaderWidget(IsWidget headerWidget);
	void setEntityPageTopWidget(IsWidget entityPageTopWidget);
	void setOpenTeamInvitesWidget(IsWidget openTeamInvitesWidgetWidget);
	void hideLoading();
	void setSynAlertWidget(IsWidget synAlert);
	void showAccessDependentMessage();
	void showOpenTeamInvites();
	void showEntityPageTop();
	void hideAccessDependentMessage();
	void hideOpenTeamInvites();
	void hideEntityPageTop();
}
