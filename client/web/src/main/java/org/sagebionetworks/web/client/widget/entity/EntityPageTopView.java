package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityPageTopView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void setEntityBundle(EntityBundle bundle, String entityTypeDisplay, boolean isAdmin, boolean canEdit);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		PlaceChanger getPlaceChanger();

		void refresh();

		void fireEntityUpdatedEvent();

		boolean isLocationable();

		boolean isLoggedIn();

		String getRstudioUrl();

		void saveRStudioUrlBase(String value);

		String getRstudioUrlBase();

	}
}
