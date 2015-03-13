package org.sagebionetworks.web.client.widget.entity.menu;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.UploadView;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ActionMenuView extends IsWidget, SynapseView, UploadView {

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
	 * @param isInTestMode 
	 */
	public void createMenu(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController,
			Long versionNumber, 
			boolean isInTestMode);
	
	void showAddDescriptionCommand(Callback onClick);
	void hideAddDescriptionCommand();
	void setEvaluationSubmitterWidget(Widget widget);
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

		boolean isUserLoggedIn();
		
		public void addNewChild(EntityType type, String parentId);

		void createLink(String selectedEntityId);

		void uploadToGenomespace();
		
		void showAvailableEvaluations();
		
		void callbackIfCertifiedIfEnabled(Callback callback);
	}

}
