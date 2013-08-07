package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationAccessControlListEditorView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Sets the details needed to display the form
	 */
	public void buildWindow(boolean unsavedChanges);
	
	/**
	 * Add an ACL Entry to the permissions dialog
	 * 
	 * @param entry
	 */
	public void addAclEntry(AclEntry entry);
	
	public void setIsOpenParticipation(Boolean isOpenParticipation);
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
		 * Push ACL changes to Synapse.
		 */
		void pushChangesToSynapse(final AsyncCallback<Void> changesPushedCallback);
		
		/**
		 * The view tells the presenter that there are unsaved changes in the view
		 * @param unsavedChanges
		 */
		public void setUnsavedViewChanges(boolean unsavedChanges);
	}
}
