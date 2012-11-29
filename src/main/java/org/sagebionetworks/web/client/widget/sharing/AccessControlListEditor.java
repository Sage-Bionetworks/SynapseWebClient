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
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
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
	
	private static final String ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS = "Active user permissions cannot be modified. Please select a different user.";
	private static final String ERROR_CANNOT_MODIFY_ENTITY_OWNER_PERMISSIONS = "Owner permissions cannot be modified. Please select a different user.";
	private static final String NULL_UEP_MESSAGE = "User's entity permissions are missing.";
	private static final String NULL_ACL_MESSAGE = "ACL is missing.";
	private static final String NULL_ENTITY_MESSAGE = "Entity is missing.";
	
	// Editor components
	private AccessControlListEditorView view;
	private NodeModelCreator nodeModelCreator;
	private SynapseClientAsync synapseClient;
	private UserAccountServiceAsync userAccountService;
	private JSONObjectAdapter jsonObjectAdapter;
	private AuthenticationController authenticationController;
	private boolean unsavedChanges;
	private boolean hasLocalACL_inRepo;
	private Long publicAclPrincipalId = null;
	private Long authenticatedAclPrincipalId = null;
	
	// Entity components
	private Entity entity;
	private UserEntityPermissions uep;
	private AccessControlList acl;	
	private Map<String, UserGroupHeader> userGroupHeaders;
	
	@Inject
	public AccessControlListEditor(AccessControlListEditorView view,
			SynapseClientAsync synapseClientAsync,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,
			UserAccountServiceAsync userAccountService) {
		this.view = view;
		this.synapseClient = synapseClientAsync;
		this.userAccountService = userAccountService;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.authenticationController = authenticationController;
		userGroupHeaders = new HashMap<String, UserGroupHeader>();
		view.setPresenter(this);		
	}	
	
	/**
	 * Set the entity with which this ACLEditor is associated.
	 */
	public void setResource(Entity entity) {
		if (!entity.equals(this.entity)) {
			acl = null;
			uep = null;
		}
		this.entity = entity;
	}
	
	/**
	 * Get the ID of the entity with which this ACLEditor is associated.
	 */
	public String getResourceId() {
		return entity == null ? null : entity.getId();
	}
	
	public boolean hasUnsavedChanges() {
		return unsavedChanges;
	}
	
	/**
	 * Generate the ACLEditor Widget
	 */
	public Widget asWidget() {
		refresh(new VoidCallback(){
			@Override
			public void success() {}
			@Override
			public void failure(Throwable throwable) {
				throwable.printStackTrace();					
				showErrorMessage(DisplayConstants.ERROR_ACL_RETRIEVAL_FAILED);
			}
		});
		return view.asWidget();
	}
	private void initViewPrincipalIds(){
		view.setPublicPrincipalId(publicAclPrincipalId);
		view.setAuthenticatedPrincipalId(authenticatedAclPrincipalId);
	}
	
	/**
	 * Refresh the ACLEditor by fetching from Synapse
	 */
	private void refresh(final VoidCallback callback) {
		if (this.entity.getId() == null) throw new IllegalStateException(NULL_ENTITY_MESSAGE);
		view.showLoading();
		if (publicAclPrincipalId == null){
			userAccountService.getPublicAndAuthenticatedGroupPrincipalIds(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					if (result != null && result.length() > 0) {
						String[] principalIds = result.split(",");
						if (principalIds.length ==2){
							publicAclPrincipalId = Long.parseLong(principalIds[0]);
							authenticatedAclPrincipalId = Long.parseLong(principalIds[1]);
							initViewPrincipalIds();
						}
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					showErrorMessage("Could not find the public group: " + caught.getMessage());
				}
			});
		}
		else {
			initViewPrincipalIds();
		}
			
		int partsMask = EntityBundleTransport.ACL | EntityBundleTransport.PERMISSIONS;
		synapseClient.getEntityBundle(entity.getId(), partsMask, new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport bundle) {
				try {
					// retrieve ACL and user entity permissions from bundle
					acl = nodeModelCreator.createJSONEntity(bundle.getAclJson(), AccessControlList.class);
					uep = nodeModelCreator.createJSONEntity(bundle.getPermissionsJson(), UserEntityPermissions.class);
					fetchUserGroupHeaders(new VoidCallback() {
						// fetch UserGroup headers for members of ACL
						@Override
						public void success() {
							// update the view
							setViewDetails();
							unsavedChanges = false;
							hasLocalACL_inRepo = (acl.getId().equals(entity.getId()));
							callback.success();
						}
						@Override
						public void failure(Throwable t) {
							onFailure(t);							
						}						
					});					
				} catch (Throwable e) {
					onFailure(e);					
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.failure(caught);
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
		view.buildWindow(isInherited, canEnableInheritance, unsavedChanges);
		populateAclEntries();
		updateIsPublicAccess();
	}

	private void updateIsPublicAccess(){
		view.setIsPubliclyVisible(uep.getCanPublicRead());	
	}
	
	private void populateAclEntries() {
		for (final ResourceAccess ra : acl.getResourceAccess()) {
			final String principalId = ra.getPrincipalId().toString();
			UserGroupHeader header = userGroupHeaders.get(principalId);
			final boolean isOwner = (ra.getPrincipalId().equals(uep.getOwnerPrincipalId()));
			if (header != null) {
				view.addAclEntry(new AclEntry(header, ra.getAccessType(), isOwner));
			} else {
				showErrorMessage("Could not find user " + principalId);
			}
		}
	}

	private void fetchUserGroupHeaders(final VoidCallback callback) {
		List<String> ids = new ArrayList<String>();
		for (ResourceAccess ra : acl.getResourceAccess())
			ids.add(ra.getPrincipalId().toString());
		synapseClient.getUserGroupHeadersById(ids, new AsyncCallback<EntityWrapper>(){
			@Override
			public void onSuccess(EntityWrapper wrapper) {
				try {	
					UserGroupHeaderResponsePage response = nodeModelCreator.createJSONEntity(wrapper.getEntityJson(), UserGroupHeaderResponsePage.class);
					for (UserGroupHeader ugh : response.getChildren())
						userGroupHeaders.put(ugh.getOwnerId(), ugh);
					if (callback != null)
						callback.success();
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (callback != null)
					callback.failure(caught);
			}
		});
	}

	@Override
	public void setAccess(Long principalId, PermissionLevel permissionLevel) {
		validateEditorState();
		String currentUserId = getCurrentUserId();
		if (uep != null && principalId.equals(uep.getOwnerPrincipalId())) {
			showErrorMessage(ERROR_CANNOT_MODIFY_ENTITY_OWNER_PERMISSIONS);
			return;
		} else if (currentUserId != null && principalId.toString().equals(currentUserId)) {
			showErrorMessage(ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS);
			return;
		}
		if (principalId.equals(publicAclPrincipalId))
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
			fetchUserGroupHeaders(new VoidCallback() {
				// fetch UserGroup headers for members of ACL
				@Override
				public void success() {
					// update the view
					setViewDetails();
				}
				@Override
				public void failure(Throwable t) {
					// update the view anyway - will fetch individual Profiles					
					setViewDetails();
				}						
			});
		} else {
			// Existing entry in the ACL
			toSet.setAccessType(AclUtils.getACCESS_TYPEs(permissionLevel));
			unsavedChanges = true;
			setViewDetails();
		}
		unsavedChanges = true;
	}

	@Override
	public void removeAccess(Long principalIdToRemove) {
		validateEditorState();
		String currentUserId = getCurrentUserId();
		if (uep != null && principalIdToRemove.equals(uep.getOwnerPrincipalId())) {
			showErrorMessage(ERROR_CANNOT_MODIFY_ENTITY_OWNER_PERMISSIONS);
			return;
		} else if (currentUserId != null && principalIdToRemove.toString().equals(currentUserId)) {
			showErrorMessage(ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS);
			return;
		}
		if (principalIdToRemove.equals(publicAclPrincipalId))
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
			unsavedChanges = true;
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
		unsavedChanges = true;
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
		synapseClient.getNodeAcl(entity.getParentId(), new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper wrapper) {
				try {
					acl = nodeModelCreator.createJSONEntity(wrapper.getEntityJson(), AccessControlList.class);
					unsavedChanges = hasLocalACL_inRepo;
					fetchUserGroupHeaders(new VoidCallback() {
						// fetch UserGroup headers for members of ACL
						@Override
						public void success() {
							// update the view
							setViewDetails();
						}
						@Override
						public void failure(Throwable t) {
							// update the view anyway - will fetch individual Profiles					
							setViewDetails();
						}						
					});					
				} catch (Throwable e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				showErrorMessage("Unable to fetch benefactor permissions.");
			}
		});
	}
	
	@Override
	public void pushChangesToSynapse(boolean recursive, final AsyncCallback<EntityWrapper> changesPushedCallback) {
		// TODO: Make recursive option for "Create"
		if (!unsavedChanges) {
			showErrorMessage("No changes have been made");
			return;
		}
		validateEditorState();
		
		// Wrap the current ACL
		EntityWrapper aclEW = null;
		try {
			JSONObjectAdapter aclJson = acl.writeToJSONObject(jsonObjectAdapter.createNew());
			aclEW = new EntityWrapper(aclJson.toJSONString(), AccessControlList.class.getName());
		} catch (JSONObjectAdapterException e) {
			showErrorMessage(DisplayConstants.ERROR_LOCAL_ACL_CREATION_FAILED);
			return;
		}
		
		// Create an async callback to receive the updated ACL from Synapse
		AsyncCallback<EntityWrapper> callback = new AsyncCallback<EntityWrapper>(){
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					acl = nodeModelCreator.createJSONEntity(result.getEntityJson(), AccessControlList.class);
					hasLocalACL_inRepo = (acl.getId().equals(entity.getId()));
					unsavedChanges = false;					
					setViewDetails();
					view.showInfoSuccess("Success", "Permissions were successfully saved to Synapse");
					changesPushedCallback.onSuccess(result);
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showInfoError("Error", "Permissions were not saved to Synapse");				
				changesPushedCallback.onFailure(caught);
			}
		};
		
		// Apply changes
		boolean hasLocalACL_inPortal = (acl.getId().equals(entity.getId()));
		
		if (hasLocalACL_inPortal && !hasLocalACL_inRepo) {
			// Local ACL exists in Portal, but does not exist in Repo
			// Create local ACL in Repo
			synapseClient.createAcl(aclEW, callback);
		} else if (hasLocalACL_inPortal && hasLocalACL_inRepo) {
			// Local ACL exists in both Portal and Repo
			// Apply updates to local ACL in Repo
			synapseClient.updateAcl(aclEW, recursive, callback);				
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
	
	/**
	 * @return the uep associated with the entity
	 */
	public UserEntityPermissions getUserEntityPermissions() {
		return uep;
	}
	
	private String getCurrentUserId() {
		return authenticationController.getLoggedInUser().getProfile().getOwnerId();
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

	interface Callback<T> {
		void success(T data);
		void failure(Throwable t);
	}
	
	interface VoidCallback {
		void success();
		void failure(Throwable t);
	}
	
	class VoidCallbackAdapter implements VoidCallback {
		@Override
		public void success() {};
		@Override
		public void failure(Throwable t) {
			if (t instanceof RuntimeException) 
				throw (RuntimeException) t; 
			else throw new RuntimeException(t);
		}
	}	
}
