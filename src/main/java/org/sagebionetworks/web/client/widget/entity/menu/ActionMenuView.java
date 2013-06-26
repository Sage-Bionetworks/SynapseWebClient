package org.sagebionetworks.web.client.widget.entity.menu;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.user.client.ui.IsWidget;

public interface ActionMenuView extends IsWidget, SynapseView {

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
			boolean isAdministrator, 
			boolean canEdit, 
			Long versionNumber, 
			boolean isInTestMode);
	
	void popupEvaluationSelector(List<Evaluation> list, List<String> submitterAliases);
	
	void showSubmitToChallengeButton();
	
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
		
		void submitToEvaluations(List<String> evaluationIds, String submitterAlias);
		
		void showAvailableEvaluations();
		
		void isSubmitButtonVisible();
	}

}
