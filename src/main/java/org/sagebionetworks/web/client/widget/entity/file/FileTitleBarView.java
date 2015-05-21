package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileTitleBarView extends IsWidget, SynapseView {

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
	public void createTitlebar(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController);

	void setLoginInstructions(String instructions);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void fireEntityUpdatedEvent(EntityUpdatedEvent event);
		
		boolean isUserLoggedIn();
		
		
		void queryForSftpLoginInstructions(String directDownloadUrl);

		/**
		 * A file that is stored in S3 could be in Synapse Storage or in a private S3 bucket.
		 * This method is used to set the correct location of a file in S3. 
		 * Therefore, this method should only be called for an entity that is in S3.
		 */
		void setS3Description(CallbackP<String> callbackP);
	}

}
