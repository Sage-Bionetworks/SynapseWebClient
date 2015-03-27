package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Editor dialog to view and modify the Access Control List of a given Entity.
 * This class is the Presenter in the MVP design pattern.
 * 
 * @author bkng
 */
public class AccessControlListEditor implements AccessControlListEditorView.Presenter {
	
	private static final String ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS = "Current user permissions cannot be modified. Please select a different user.";
	private static final String NULL_UEP_MESSAGE = "User's entity permissions are missing.";
	private static final String NULL_ACL_MESSAGE = "ACL is missing.";
	private static final String NULL_ENTITY_MESSAGE = "Entity is missing.";
	
	// Editor components
	private AccessControlListEditorView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private boolean unsavedViewChanges;
	private boolean hasLocalACL_inRepo;
	GlobalApplicationState globalApplicationState;
	PublicPrincipalIds publicPrincipalIds;
	GWTWrapper gwt;
	
	// Entity components
	private Entity entity;
	private boolean canChangePermission;
	private UserEntityPermissions uep;
	private AccessControlList acl;	
	private Map<String, UserGroupHeader> userGroupHeaders;
	private Set<String> originalPrincipalIdSet;
	HasChangesHandler hasChangesHandler;
	
	@Inject
	public AccessControlListEditor(AccessControlListEditorView view,
			SynapseClientAsync synapseClientAsync,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			GWTWrapper gwt) {
		this.view = view;
		this.synapseClient = synapseClientAsync;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.gwt = gwt;
		
		userGroupHeaders = new HashMap<String, UserGroupHeader>();
		view.setPresenter(this);
		view.setPermissionsToDisplay(getPermList(), getPermissionsToDisplay());
		publicPrincipalIds = new PublicPrincipalIds();
		publicPrincipalIds.setPublicAclPrincipalId(Long.parseLong(globalApplicationState.getSynapseProperty(WebConstants.PUBLIC_ACL_PRINCIPAL_ID)));
		publicPrincipalIds.setAnonymousUserId(Long.parseLong(globalApplicationState.getSynapseProperty(WebConstants.ANONYMOUS_USER_PRINCIPAL_ID)));
		publicPrincipalIds.setAuthenticatedAclPrincipalId(Long.parseLong(globalApplicationState.getSynapseProperty(WebConstants.AUTHENTICATED_ACL_PRINCIPAL_ID)));
		initViewPrincipalIds();
	}

	
	/**
	 * Configure this widget before using it.
	 * Set the entity with which this ACLEditor is associated.
	 */
	public void configure(Entity entity, boolean canChangePermission, HasChangesHandler hasChangesHandler) {
		if (!entity.equals(this.entity)) {
			acl = null;
			uep = null;
		}
		this.hasChangesHandler = hasChangesHandler;
		this.entity = entity;
		this.canChangePermission = canChangePermission;
	}
	
	/**
	 * Get the ID of the entity with which this ACLEditor is associated.
	 */
	public String getResourceId() {
		return entity == null ? null : entity.getId();
	}
	
	public void setUnsavedViewChanges(boolean unsavedViewChanges) {
		this.unsavedViewChanges = unsavedViewChanges;
	}
	
	/**
	 * Generate the ACLEditor Widget
	 */
	public Widget asWidget() {
		return view.asWidget();
	}
	private void initViewPrincipalIds(){
		if (publicPrincipalIds != null) {
			view.setPublicAclPrincipalId(publicPrincipalIds.getPublicAclPrincipalId());
		}
	}
	public PermissionLevel[] getPermList() {
		return new PermissionLevel[] {PermissionLevel.CAN_VIEW, PermissionLevel.CAN_EDIT, PermissionLevel.CAN_EDIT_DELETE, PermissionLevel.CAN_ADMINISTER};
	}
	public HashMap<PermissionLevel, String> getPermissionsToDisplay() {
		HashMap<PermissionLevel, String> permissionDisplay = new HashMap<PermissionLevel, String>();
		permissionDisplay.put(PermissionLevel.CAN_VIEW, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_VIEW);
		permissionDisplay.put(PermissionLevel.CAN_EDIT, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_EDIT);
		permissionDisplay.put(PermissionLevel.CAN_EDIT_DELETE, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_EDIT_DELETE);
		permissionDisplay.put(PermissionLevel.CAN_ADMINISTER, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_ADMINISTER);		
		permissionDisplay.put(PermissionLevel.OWNER, DisplayConstants.MENU_PERMISSION_LEVEL_IS_OWNER);
		return permissionDisplay;
	}
	
	/**
	 * Refresh the ACLEditor by fetching from Synapse
	 */
	private void refresh(final AsyncCallback<Void> callback) {
		if (this.entity.getId() == null) throw new IllegalStateException(NULL_ENTITY_MESSAGE);
		view.showLoading();
		hasChangesHandler.hasChanges(false);
		
		int partsMask = EntityBundle.BENEFACTOR_ACL | EntityBundle.PERMISSIONS;
		synapseClient.getEntityBundle(entity.getId(), partsMask, new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				try {
					// retrieve ACL and user entity permissions from bundle
					acl = bundle.getBenefactorAcl();
					uep = bundle.getPermissions();
					//initialize original principal id set
					originalPrincipalIdSet = new HashSet<String>();
					for (final ResourceAccess ra : acl.getResourceAccess()) {
						final String principalId = ra.getPrincipalId().toString();
						originalPrincipalIdSet.add(principalId);
					}
					//default notification to true
					view.setIsNotifyPeople(true);
					fetchUserGroupHeaders(new AsyncCallback<Void>() {
						public void onSuccess(Void result) {
							// update the view
							setViewDetails();
							hasChangesHandler.hasChanges(false);
							hasLocalACL_inRepo = (acl.getId().equals(entity.getId()));
							callback.onSuccess(null);
						};
						public void onFailure(Throwable caught) {
							onFailure(caught);
						};
					});				
				} catch (Throwable e) {
					onFailure(e);					
				}
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
		boolean isInherited = !acl.getId().equals(entity.getId());
		boolean canEnableInheritance = uep.getCanEnableInheritance();
		view.buildWindow(isInherited, canEnableInheritance, canChangePermission);
		populateAclEntries();
		updateIsPublicAccess();
	}

	private void updateIsPublicAccess(){
		view.setIsPubliclyVisible(PublicPrivateBadge.isPublic(acl, publicPrincipalIds));	
	}
	
	@Override
	public void makePrivate() {
		//try to remove the public principal ids from the acl
		List<Long> toRemove = new ArrayList<Long>();
		for (ResourceAccess ra : acl.getResourceAccess()) {
			if (publicPrincipalIds.getAuthenticatedAclPrincipalId().equals(ra.getPrincipalId())){
				toRemove.add(publicPrincipalIds.getAuthenticatedAclPrincipalId());
			} else if (publicPrincipalIds.getPublicAclPrincipalId().equals(ra.getPrincipalId())) {
				toRemove.add(publicPrincipalIds.getPublicAclPrincipalId());
			} else if (publicPrincipalIds.getAnonymousUserPrincipalId().equals(ra.getPrincipalId())) {
				toRemove.add(publicPrincipalIds.getAnonymousUserPrincipalId());
			}
		}
		for (Long id : toRemove) {
			removeAccess(id);	
		}
	}
	
	private void populateAclEntries() {
		
		for (final ResourceAccess ra : acl.getResourceAccess()) {
			final String principalId = ra.getPrincipalId().toString();
			final UserGroupHeader header = userGroupHeaders.get(principalId);
			final boolean isOwner = (ra.getPrincipalId().equals(uep.getOwnerPrincipalId()));
			if (header != null) {
				String title = header.getIsIndividual() ? DisplayUtils.getDisplayName(header.getFirstName(), header.getLastName(), header.getUserName()) : 
					header.getUserName();
				view.addAclEntry(new AclEntry(principalId, ra.getAccessType(), isOwner, title, "", header.getIsIndividual()));
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
		String currentUserId = getCurrentUserId();
		if (currentUserId != null && principalId.toString().equals(currentUserId)) {
			showErrorMessage(ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS);
			return;
		}
		if (principalId.equals(publicPrincipalIds.getPublicAclPrincipalId()))
			uep.setCanPublicRead(true);
		
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
		String currentUserId = getCurrentUserId();
		if (currentUserId != null && principalIdToRemove.toString().equals(currentUserId)) {	
			showErrorMessage(ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS);
			return;
		}
		if (principalIdToRemove.equals(publicPrincipalIds.getPublicAclPrincipalId()))
			uep.setCanPublicRead(false);
		boolean foundUser = false;;
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
		validateEditorState();
		if (acl.getId().equals(entity.getId())) {
			showErrorMessage("Entity already has an ACL!");
			return;
		}		
		acl.setId(entity.getId());
		acl.setCreationDate(new Date());		
		hasChangesHandler.hasChanges(true);
		setViewDetails();
	}
	
	@Override
	public void deleteAcl() {
		if (!acl.getId().equals(entity.getId())) {
			showErrorMessage("Cannot delete an inherited ACL!");
			return;
		}
		if (!uep.getCanEnableInheritance()) {
			// Parent is root
			showErrorMessage("Cannot enable inheritance on this entity!");
			return;
		}		
		// Fetch parent's benefactor's ACL (candidate benefactor for this entity)
		synapseClient.getEntityBenefactorAcl(entity.getParentId(), new AsyncCallback<AccessControlList>() {
			@Override
			public void onSuccess(AccessControlList result) {
				try {
					acl = result;
					hasChangesHandler.hasChanges(hasLocalACL_inRepo);
					fetchUserGroupHeaders(updateViewDetailsCallback());					
				} catch (Throwable e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					showErrorMessage("Unable to fetch benefactor permissions.");
			}
		});
	}
	
	public void pushChangesToSynapse(final boolean recursive, final Callback changesPushedCallback) {
		if(unsavedViewChanges) {
			view.alertUnsavedViewChanges(new Callback() {
				
				@Override
				public void invoke() {
					pushChangesToSynapse(recursive, changesPushedCallback);
				}
			});
			return;
		}
		
		validateEditorState();
		
		// Create an async callback to receive the updated ACL from Synapse
		AsyncCallback<AccessControlList> callback = new AsyncCallback<AccessControlList>(){
			@Override
			public void onSuccess(AccessControlList result) {
				acl = result;
				hasLocalACL_inRepo = (acl.getId().equals(entity.getId()));				
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
		
		applyChanges(recursive, callback);
	}
	
	protected void applyChanges(boolean recursive, AsyncCallback<AccessControlList> callback) {
		// Apply changes
		boolean hasLocalACL_inPortal = (acl.getId().equals(entity.getId()));
		
		if (hasLocalACL_inPortal && !hasLocalACL_inRepo) {
			// Local ACL exists in Portal, but does not exist in Repo
			// Create local ACL in Repo
			synapseClient.createAcl(acl, callback);
			notifyNewUsers();
		} else if (hasLocalACL_inPortal && hasLocalACL_inRepo) {
			// Local ACL exists in both Portal and Repo
			// Apply updates to local ACL in Repo
			synapseClient.updateAcl(acl, recursive, callback);
			notifyNewUsers();
		} else if (!hasLocalACL_inPortal && hasLocalACL_inRepo) {
			// Local ACL does not exist in Portal but does exist in Repo
			// Delete local ACL in Repo
			synapseClient.deleteAcl(entity.getId(), callback);
		} else { /* (!hasLocal_inPortal && !isInherited_inSynapse) */
			// Local ACL does not exist in both Portal and Repo
			// Do not modify Repo
			throw new IllegalStateException("Cannot modify an inherited ACL.");
		}
	}
	public void notifyNewUsers() {
		if (view.isNotifyPeople()) {
			//create the principal id set
			HashSet<String> newPrincipalIdSet = new HashSet<String>();
			for (ResourceAccess ra : acl.getResourceAccess()) {
				//SWC-1195: only notify individuals (not teams)
				UserGroupHeader header = userGroupHeaders.get(ra.getPrincipalId().toString());
				if (header != null && header.getIsIndividual()) {
					newPrincipalIdSet.add(ra.getPrincipalId().toString());
				}
			}
			//now remove all of the original entries
			for (String principalId : originalPrincipalIdSet) {
				newPrincipalIdSet.remove(principalId);
			}
			//never try to notify all users
			newPrincipalIdSet.remove(publicPrincipalIds.getAnonymousUserPrincipalId().toString());
			newPrincipalIdSet.remove(publicPrincipalIds.getAuthenticatedAclPrincipalId().toString());
			newPrincipalIdSet.remove(publicPrincipalIds.getPublicAclPrincipalId().toString());
			
			if (newPrincipalIdSet.size() > 0) {
				//now send a message to these users
				String message = DisplayUtils.getShareMessage(getDisplayName(getCurrentUserId()), entity.getId(), gwt.getHostPageBaseURL());
				String subject = entity.getName() + DisplayConstants.SHARED_ON_SYNAPSE_SUBJECT;
				synapseClient.sendMessage(newPrincipalIdSet, subject, message, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
					}
					@Override
					public void onFailure(Throwable caught) {
						if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)){
							view.showErrorMessage(caught.getMessage());
						}
					}
				});
			}
		}
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
	 * @return the uep associated with the entity
	 */
	public UserEntityPermissions getUserEntityPermissions() {
		return uep;
	}
	
	private String getCurrentUserId() {
		return authenticationController.getCurrentUserPrincipalId();
	}

	/**
	 * Ensure the editor has a valid Entity ID, ACL, and User Permissions.
	 */
	private void validateEditorState() {
		if (this.entity.getId() == null) throw new IllegalStateException(NULL_ENTITY_MESSAGE);
		if (this.acl == null) throw new IllegalStateException(NULL_ACL_MESSAGE);
		if (this.uep == null) throw new IllegalStateException(NULL_UEP_MESSAGE);
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
				showErrorMessage(DisplayConstants.ERROR_ACL_RETRIEVAL_FAILED + ": " + caught.getMessage());
			}
		});
	}
	/**
	 * This handler is notified when there are changes made to the editor.
	 */
	public interface HasChangesHandler{
		/**
		 * Called with true then the user has changes in the editor.  Called with false when there are no changes in this editor.
		 * @param hasChanges True when there are changes.  False when there are no changes.
		 */
		void hasChanges(boolean hasChanges);
		
	}
	
}
