package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;

import com.google.gwt.user.client.ui.IsWidget;

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
			
		void updateArea(EntityArea area, String areaToken);
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
	
}
