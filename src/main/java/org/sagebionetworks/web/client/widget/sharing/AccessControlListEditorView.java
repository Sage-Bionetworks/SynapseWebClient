package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.ui.IsWidget;

public interface AccessControlListEditorView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Sets the details needed to display the form
	 * @param entries the current ACL
	 * @param principals the available principals
	 * @param isEditable
	 */
	public void buildWindow(boolean isInherited, boolean canEnableInheritance);
	
	/**
	 * Add an ACL Entry to the permissions dialog
	 * 
	 * @param entry
	 */
	public void addAclEntry(AclEntry entry);
	
	/**
	 * Set the view to a loading state while async loads
	 */
	public void showLoading();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
		/**
		 * Create an ACL for the current entity (which otherwise inherits its ACL from an ancestor)
		 */
		void createAcl();
		
		/**
		 * Add the given principal to the ACL, with the given permission level
		 * @param principalId
		 * @param permissionLevel
		 */
		void addAccess(Long principalId, PermissionLevel permissionLevel);
		
		/**
		 * Change the access level of the given principal (already in the ACL) to the given permission level
		 * @param principalId
		 * @param permissionLevel
		 */
		void changeAccess(Long principalId, PermissionLevel permissionLevel);
		
		/**
		 * Remove the given principal from the ACL
		 * 
		 * @param principalId
		 */
		void removeAccess(Long principalId);
		
		/**
		 * Delete the ACL for the current entity, making the entity inherit its access permissions from its ancestor's ACL
		 */
		void deleteAcl();
	}
}
