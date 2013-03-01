package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityMetadataView extends IsWidget {

	public void setPresenter(Presenter p);

	public void setEntityBundle(EntityBundle bundle, boolean readOnly);

	public void showInfo(String string, String message);
	
	public void setDetailedMetadataVisible(boolean visible);
	public void setEntityNameVisible(boolean visible);

	public void showErrorMessage(String message);

	public interface Presenter {

		void loadVersions(String id, int offset, int limit,
				AsyncCallback<PaginatedResults<VersionInfo>> asyncCallback);
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

		public RESTRICTION_LEVEL getRestrictionLevel();

		public APPROVAL_TYPE getApprovalType();

		Callback accessRequirementCallback();

		Callback getImposeRestrictionsCallback();

		boolean hasFulfilledAccessRequirements();

		String accessRequirementText();

		Callback getLoginCallback();

		void fireEntityUpdatedEvent();

		void editCurrentVersionInfo(String entityId, String version, String comment);

		void promoteVersion(String entityId, Long versionNumber);

		void deleteVersion(String entityId, Long versionNumber);
		
		void setIsFavorite(boolean isFavorite);
		
		boolean isFavorite();

	}

	public void setIsFavorite(boolean isFavorite);

}
