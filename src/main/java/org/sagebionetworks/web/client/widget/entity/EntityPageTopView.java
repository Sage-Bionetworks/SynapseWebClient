package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityPageTopView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void setEntityBundle(EntityBundle bundle, String entityTypeDisplay, boolean isAdmin, boolean canEdit);
	
	/**
	 * Sets the RStudio URL for the view
	 * @param rStudioUrl
	 */
	public void setRStudioUrlReady();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void refresh();
				
		void fireEntityUpdatedEvent();

		boolean isLocationable();

		boolean isLoggedIn();

//		String getRstudioUrl();
//
//		void saveRStudioUrlBase(String value);
//
//		String getRstudioUrlBase();

		void loadShortcuts(int offset, int limit, AsyncCallback<PaginatedResults<EntityHeader>> asyncCallback);

		String createEntityLink(String id, String version, String display);
		
		ImageResource getIconForType(String typeString);

	}

}
