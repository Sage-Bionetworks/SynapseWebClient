package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.APPROVAL_REQUIRED;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityPageTopView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void setEntityBundle(EntityBundle bundle, UserProfile userProfile, String entityTypeDisplay, boolean isAdmin, boolean canEdit, boolean readOnly);
	
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

		void loadShortcuts(int offset, int limit, AsyncCallback<PaginatedResults<EntityHeader>> asyncCallback);

		String createEntityLink(String id, String version, String display);
		
		ImageResource getIconForType(String typeString);
		
		boolean isAnonymous();
		
		/**
		 * 
		 * @return
		 * @exception if anonymous
		 */
		String getJiraFlagUrl();
		
		/**
		 * 
		 */
		String getJiraRequestAccessUrl();
		
		boolean hasAdministrativeAccess();
		
		boolean includeRestrictionWidget();
		
		public APPROVAL_REQUIRED getRestrictionLevel();		
				
		Callback accessRequirementCallback();
		
		Callback getImposeRestrictionsCallback();
		
		boolean hasFulfilledAccessRequirements();
		
		String accessRequirementText();
		
		boolean isTermsOfUseAccessRequirement();

		Callback getLoginCallback();

	}


}
