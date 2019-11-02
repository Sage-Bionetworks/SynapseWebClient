package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;
import com.google.gwt.user.client.ui.IsWidget;

public interface AccessControlListEditorView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Sets the details needed to display the form
	 * 
	 * @param entries the current ACL
	 * @param principals the available principals
	 * @param isEditable
	 */
	public void buildWindow(boolean isProject, boolean isInherited, String aclEntityId, boolean canEnableInheritance, boolean canChangePermission, PermissionLevel defaultPermissionLevel, boolean isLoggedIn);

	/**
	 * Add an ACL Entry to the permissions dialog
	 * 
	 * @param entry
	 */
	public void addAclEntry(AclEntry entry);

	public void setIsPubliclyVisible(Boolean isPubliclyVisible);

	public void setPublicAclPrincipalId(Long publicAclPrincipalId);

	public void setAuthenticatedAclPrinciapalId(Long authenticatedPrincipalId);

	void setSynAlert(IsWidget w);

	/**
	 * Set the view to a loading state while async loads
	 */
	public void showLoading();

	void setNotifyCheckboxVisible(boolean isVisible);

	void setDeleteLocalACLButtonVisible(boolean isVisible);

	void setPublicPrivateButtonVisible(boolean isVisible);

	void showInfoSuccess(String title, String message);

	void setPermissionsToDisplay(PermissionLevel[] permList, Map<PermissionLevel, String> permissionsDisplay);

	/**
	 * true if user would like to notify newly added people that this item has been shared with them.
	 * 
	 * @return
	 */
	public Boolean isNotifyPeople();

	public void setIsNotifyPeople(Boolean value);

	/**
	 * Presenter interface
	 */
	public interface Presenter {

		/**
		 * Set the access level of the given principal. Changes are NOT pushed to Synapse.
		 * 
		 * @param principalId
		 * @param permissionLevel
		 */
		void setAccess(Long principalId, PermissionLevel permissionLevel);

		/**
		 * Remove the given principal from the ACL. Changes are NOT pushed to Synapse.
		 * 
		 * @param principalId
		 */
		void removeAccess(Long principalId);

		/**
		 * Create a local for the current entity, with permissions copied from the entity's benefactor.
		 * Changes are NOT pushed to Synapse.
		 */
		void createAcl();

		/**
		 * Delete the ACL for the current entity. The entity will subsequently inherit access permissions
		 * from its ancestor's ACL. Changes are NOT pushed to Synapse. *
		 */
		void deleteAcl();

		void makePrivate();
	}
}
