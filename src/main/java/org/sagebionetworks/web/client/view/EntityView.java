package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.model.EntityBundle;

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
			
	}

	/**
	 * Set entity to display
	 * @param versionNumber to highlight
	 * @param entity
	 * @param entityMetadata 
	 */
	public void setEntityBundle(EntityBundle bundle, Long versionNumber);

	public void show404();
	
	public void show403();
	
}
