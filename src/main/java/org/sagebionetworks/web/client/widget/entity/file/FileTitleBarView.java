package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
		
		public void addNewChild(EntityType type, String parentId);
		
		void queryForSftpLoginInstructions(String directDownloadUrl);
	}

}
