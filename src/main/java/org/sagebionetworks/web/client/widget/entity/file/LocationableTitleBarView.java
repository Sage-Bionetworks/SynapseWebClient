package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface LocationableTitleBarView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);


	/**
	 * Build menus for this entity
	 * @param entity
	 * @param entityType 
	 */
	public void createTitlebar(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController,
			boolean canCertifiedUserAddChild);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void fireEntityUpdatedEvent();
		
		boolean isUserLoggedIn();
		
		public void addNewChild(EntityType type, String parentId);
	}

}
