package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor.HasChangesHandler;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Editor dialog to view and modify the Access Control List of a given Team.
 *
 * @author jay
 */
public class TeamAccessControlListEditor implements AccessControlListEditorView.Presenter {
	private static final String NULL_ACL_MESSAGE = "ACL is missing.";
	
	// Editor components
	private AccessControlListEditorView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	
	GlobalApplicationState globalApplicationState;
	
	private Team team;
	private AccessControlList acl;	
	private Map<String, UserGroupHeader> userGroupHeaders;
	private Set<String> originalPrincipalIdSet;
	HasChangesHandler hasChangesHandler;
	
	@Inject
	public TeamAccessControlListEditor(AccessControlListEditorView view,
			SynapseClientAsync synapseClientAsync,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState
			) {
		this.view = view;
		this.synapseClient = synapseClientAsync;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		
		userGroupHeaders = new HashMap<String, UserGroupHeader>();
		view.setPresenter(this);
		view.setPermissionsToDisplay(getPermList(), getPermissionsToDisplay());
		view.setNotifyCheckboxVisible(false);
		view.setDeleteLocalACLButtonVisible(false);
		view.setPublicPrivateButtonVisible(false);
	}

	
	/**
	 * Configure this widget before using it.
	 * Set the entity with which this ACLEditor is associated.
	 */
	public void configure(Team team, HasChangesHandler hasChangesHandler) {
		if (!team.equals(this.team)) {
			acl = null;
		}
		this.hasChangesHandler = hasChangesHandler;
		this.team = team;
	}
	
	/**
	 * Get the ID of the entity with which this ACLEditor is associated.
	 */
	public String getResourceId() {
		return team == null ? null : team.getId();
	}
	
	public PermissionLevel[] getPermList() {
		return new PermissionLevel[] {PermissionLevel.CAN_MESSAGE_TEAM, PermissionLevel.CAN_ADMINISTER_TEAM};
	}

	public HashMap<PermissionLevel, String> getPermissionsToDisplay() {
		HashMap<PermissionLevel, String> permissionDisplay = new HashMap<PermissionLevel, String>();
		permissionDisplay.put(PermissionLevel.CAN_MESSAGE_TEAM, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_MESSAGE_TEAM);
		permissionDisplay.put(PermissionLevel.CAN_ADMINISTER_TEAM, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_ADMINISTER_TEAM);		
		return permissionDisplay;
	}
	
	/**
	 * Generate the ACLEditor Widget
	 */
	public Widget asWidget() {
		return view.asWidget();
	}
	
	/**
	 * Refresh the ACLEditor by fetching from Synapse
	 */
	private void refresh(final AsyncCallback<Void> callback) {
		view.showLoading();
		hasChangesHandler.hasChanges(false);
		
		synapseClient.getTeamAcl(team.getId(), new AsyncCallback<AccessControlList>() {
			@Override
			public void onSuccess(AccessControlList result) {
				acl = result;
				
				//initialize original principal id set
				originalPrincipalIdSet = new HashSet<String>();
				for (final ResourceAccess ra : acl.getResourceAccess()) {
					final String principalId = ra.getPrincipalId().toString();
					originalPrincipalIdSet.add(principalId);
				}
				
				fetchUserGroupHeaders(new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						// update the view
						setViewDetails();
						hasChangesHandler.hasChanges(false);
						callback.onSuccess(null);
					};
					public void onFailure(Throwable caught) {
						onFailure(caught);
					};
				});

			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	/**
	 * Send ACL details to the View.
	 */
	private void setViewDetails() {
		validateEditorState();
		view.showLoading();
		boolean isInherited = false;
		boolean canEnableInheritance = false;
		boolean canChangePermission = true;
		view.buildWindow(isInherited, canEnableInheritance, canChangePermission, PermissionLevel.CAN_MESSAGE_TEAM);
		populateAclEntries();
	}

	@Override
	public void makePrivate() {
	}
	
	private void populateAclEntries() {
		
		for (final ResourceAccess ra : acl.getResourceAccess()) {
			final String principalId = ra.getPrincipalId().toString();
			final UserGroupHeader header = userGroupHeaders.get(principalId);
			if (header != null) {
				String title = header.getIsIndividual() ? DisplayUtils.getDisplayName(header.getFirstName(), header.getLastName(), header.getUserName()) : 
					header.getUserName();
				view.addAclEntry(new AclEntry(principalId, ra.getAccessType(), false, title, "", header.getIsIndividual()));
			} else {
				showErrorMessage("Could not find user " + principalId);
			}
		}
	}
	
	private void fetchUserGroupHeaders(final AsyncCallback<Void> callback) {
		ArrayList<String> ids = new ArrayList<String>();
		for (ResourceAccess ra : acl.getResourceAccess())
			ids.add(ra.getPrincipalId().toString());
		synapseClient.getUserGroupHeadersById(ids, new AsyncCallback<UserGroupHeaderResponsePage>(){
			@Override
			public void onSuccess(UserGroupHeaderResponsePage response) {
				for (UserGroupHeader ugh : response.getChildren())
					userGroupHeaders.put(ugh.getOwnerId(), ugh);
				if (callback != null)
					callback.onSuccess(null);
			}
			@Override
			public void onFailure(Throwable caught) {
				if (callback != null)
					callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public void setAccess(Long principalId, PermissionLevel permissionLevel) {
		validateEditorState();
		ResourceAccess toSet = null;
		for (ResourceAccess ra : acl.getResourceAccess()) {
			if (ra.getPrincipalId().equals(principalId))
				toSet = ra;
		}
		if (toSet == null) {
			// New entry in the ACL - need to fetch a new header
			toSet = new ResourceAccess();
			toSet.setPrincipalId(principalId);
			acl.getResourceAccess().add(toSet);
			toSet.setAccessType(AclUtils.getACCESS_TYPEs(permissionLevel));
			fetchUserGroupHeaders(updateViewDetailsCallback());
		} else {
			// Existing entry in the ACL
			toSet.setAccessType(AclUtils.getACCESS_TYPEs(permissionLevel));
			setViewDetails();
		}
		hasChangesHandler.hasChanges(true);
	}

	private AsyncCallback<Void> updateViewDetailsCallback() {
		return new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				// update the view
				setViewDetails();
			}
			@Override
			public void onFailure(Throwable caught) {
				// update the view anyway - will fetch individual Profiles					
				setViewDetails();
			}
		};
	}
	
	@Override
	public void removeAccess(Long principalIdToRemove) {
		validateEditorState();
		
		boolean foundUser = false;
		Set<ResourceAccess> newRAs = new HashSet<ResourceAccess>();
		for (ResourceAccess ra : acl.getResourceAccess()) {
			if (!ra.getPrincipalId().equals(principalIdToRemove)) {
				newRAs.add(ra);
			} else {				
				foundUser = true;
			}
		}
		if (foundUser) {
			acl.setResourceAccess(newRAs);
			hasChangesHandler.hasChanges(true);
			setViewDetails();
		} else {
			// not found
			showErrorMessage("ACL does not have a record for " + principalIdToRemove);
		}
	}

	@Override
	public void createAcl() {
	}
	
	@Override
	public void deleteAcl() {
	}
	
	public void pushChangesToSynapse(final Callback changesPushedCallback) {
		validateEditorState();
		
		// Create an async callback to receive the updated ACL from Synapse
		AsyncCallback<AccessControlList> callback = new AsyncCallback<AccessControlList>(){
			@Override
			public void onSuccess(AccessControlList result) {
				acl = result;
				setViewDetails();
				view.showInfoSuccess("Success", "Permissions were successfully saved to Synapse");
				changesPushedCallback.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);
				view.showInfoError("Error", "Permissions were not saved to Synapse");
				hasChangesHandler.hasChanges(true);
			}
		};
		
		applyChanges(callback);
	}
	
	protected void applyChanges(AsyncCallback<AccessControlList> callback) {
		synapseClient.updateTeamAcl(acl, callback);
	}
	
	public String getDisplayName(String principalId) {
		//get the user group header for this resource
		UserGroupHeader header = userGroupHeaders.get(principalId);
		if (header != null) {
			return DisplayUtils.getDisplayName(header);
		}
		return "(Unknown user)";
	}

	/**
	 * Ensure the editor has a valid Entity ID, ACL, and User Permissions.
	 */
	private void validateEditorState() {
		if (this.acl == null) throw new IllegalStateException(NULL_ACL_MESSAGE);
	}
	
	private void showErrorMessage(String s) {
		view.showErrorMessage(s);
	}

	public void refresh() {
		refresh(new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
			}

			@Override
			public void onFailure(Throwable caught) {
				showErrorMessage(DisplayConstants.ERROR_ACL_RETRIEVAL_FAILED);
			}
		});
	}
}
