package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface MyEntitiesBrowserView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setMyEntities(List<EntityHeader> rootEntities);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		PlaceChanger getPlaceChanger();

		void entitySelected(String selectedEntityId);

		public void loadUserUpdateable();
		
		void createdOnlyFilter();

	}
}
