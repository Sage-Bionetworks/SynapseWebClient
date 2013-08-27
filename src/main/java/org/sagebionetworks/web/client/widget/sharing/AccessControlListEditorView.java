package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface AccessControlListEditorView extends IsWidget, SynapseView {

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
	public void buildWindow(boolean isInherited, boolean canEnableInheritance, boolean unsavedChanges);
	
	/**
	 * Add an ACL Entry to the permissions dialog
	 * 
	 * @param entry
	 */
	public void addAclEntry(AclEntry entry);
	
	public void setIsPubliclyVisible(Boolean isPubliclyVisible);
	public void setPublicPrincipalId(Long id);
	public void setAuthenticatedPrincipalId(Long id);
	
	/**
	 * Set the view to a loading state while async loads
	 */
	public void showLoading();
	
	void showInfoError(String title, String message);

	void showInfoSuccess(String title, String message);

	/**
	 * Prompt about unsaved view changes
	 * @param saveCallback 
	 */
	public void alertUnsavedViewChanges(Callback saveCallback);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
		/**
		 * Set the access level of the given principal. Changes are NOT pushed
		 * to Synapse.
		 * 
		 * @param principalId
		 * @param permissionLevel
		 */
		void setAccess(Long principalId, PermissionLevel permissionLevel);

		/**
		 * Remove the given principal from the ACL. Changes are NOT pushed to 
		 * Synapse.
		 * 
		 * @param principalId
		 */
		void removeAccess(Long principalId);

		/**
		 * Create a local for the current entity, with permissions copied from 
		 * the entity's benefactor. Changes are NOT pushed to Synapse.
		 */
		void createAcl();
		
		/**
		 * Delete the ACL for the current entity. The entity will subsequently 
		 * inherit access permissions from its ancestor's ACL. Changes are NOT
		 * pushed to Synapse.		 * 
		 */
		void deleteAcl();

		/**
		 * Push ACL changes to Synapse.
		 * 
		 * If 'recursive' is true, then all ACLs for all descendant entities
		 * will be deleted in Synapse. These descendant entities will 
		 * consequently inherit access permissions from this entity's ACL.
		 */
		void pushChangesToSynapse(boolean recursive, final AsyncCallback<EntityWrapper> changesPushedCallback);
		
		/**
		 * The view tells the presenter that there are unsaved changes in the view
		 * @param unsavedChanges
		 */
		public void setUnsavedViewChanges(boolean unsavedChanges);
		
		void makePrivate();
	}
}
