package org.sagebionetworks.web.client.widget.entity.menu;

import java.util.List;

import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.user.client.ui.IsWidget;

public interface ActionMenuView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);


	/**
	 * Build menus for this entity
	 * @param entity
	 * @param entityType 
	 * @param canEdit 
	 * @param isAdministrator 
	 * @param readOnly 
	 */
	public void createMenu(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController,
			boolean isAdministrator, 
			boolean canEdit, 
			boolean readOnly);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void fireEntityUpdatedEvent();
		
		/**
		 * Called when the edit button is pushed.
		 */
		void onEdit();

		void deleteEntity();
		
		/**
		 * Move an entity by assigning it a new parent entity.
		 */
		void moveEntity(String newParentId);

		List<EntityType> getAddSkipTypes();

		boolean isUserLoggedIn();
		
		public void addNewChild(EntityType type, String parentId);

		void createLink(String selectedEntityId);

	}

}
