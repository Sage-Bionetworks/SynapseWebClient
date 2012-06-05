package org.sagebionetworks.web.client.widget.sharing;

import java.util.Collection;
import java.util.List;

import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclPrincipal;
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
	public void setAclDetails(Collection<AclEntry> entries, Collection<AclPrincipal> principals, boolean isEditable);
	
	/**
	 * Set the view to a loading state while async loads
	 */
	public void showLoading();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
		void createAcl();
		
		void addAccess(Long principalId, PermissionLevel permissionLevel);
		
		void changeAccess(Long principalId, PermissionLevel permissionLevel);
		
		void removeAccess(Long principalId);
		
		void deleteAcl();
	}
}
