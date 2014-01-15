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
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
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
	
	private static final String ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS = "Current user permissions cannot be modified. Please select a different user.";
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
	private boolean unsavedViewChanges;
	private boolean hasLocalACL_inRepo;
	GlobalApplicationState globalApplicationState;
	PublicPrincipalIds publicPrincipalIds;
	GWTWrapper gwt;
	
	private AdapterFactory adapterFactory;
	
	// Entity components
	private Entity entity;
	private UserEntityPermissions uep;
	private AccessControlList acl;	
	private Map<String, UserGroupHeader> userGroupHeaders;
	private Set<String> originalPrincipalIdSet;
	
	@Inject
	public AccessControlListEditor(AccessControlListEditorView view,
			SynapseClientAsync synapseClientAsync,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,
			UserAccountServiceAsync userAccountService,
			GlobalApplicationState globalApplicationState,
			GWTWrapper gwt,
			AdapterFactory adapterFactory) {
		this.view = view;
		this.synapseClient = synapseClientAsync;
		this.userAccountService = userAccountService;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.gwt = gwt;
		this.adapterFactory = adapterFactory;
		
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
		return unsavedChanges || unsavedViewChanges;
	}
	
	public void setUnsavedViewChanges(boolean unsavedViewChanges) {
		this.unsavedViewChanges = unsavedViewChanges;
	}
	
	/**
	 * Generate the ACLEditor Widget
	 */
	public Widget asWidget() {
		refresh(new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
			}
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();					
				showErrorMessage(DisplayConstants.ERROR_ACL_RETRIEVAL_FAILED);
			}
		});
		return view.asWidget();
	}
	private void initViewPrincipalIds(){
		if (publicPrincipalIds != null) {
			view.setPublicPrincipalIds(publicPrincipalIds);
		}
	}
	
	/**
	 * Refresh the ACLEditor by fetching from Synapse
	 */
	private void refresh(final AsyncCallback<Void> callback) {
		if (this.entity.getId() == null) throw new IllegalStateException(NULL_ENTITY_MESSAGE);
		view.showLoading();
		if (publicPrincipalIds == null){
			userAccountService.getPublicAndAuthenticatedGroupPrincipalIds(new AsyncCallback<PublicPrincipalIds>() {
				@Override
				public void onSuccess(PublicPrincipalIds result) {
					publicPrincipalIds = result;
					initViewPrincipalIds();
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
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
							unsavedChanges = false;
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
		view.buildWindow(isInherited, canEnableInheritance, unsavedChanges);
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
				if (header.getIsIndividual()) {
					AccessControlListEditor.getUserProfile(principalId, synapseClient, adapterFactory, new AsyncCallback<UserProfile>() {
						@Override
						public void onSuccess(UserProfile profile) {
							view.addAclEntry(new AclEntry(principalId, ra.getAccessType(), isOwner, profile));
						}
						@Override
						public void onFailure(Throwable caught) {
							//unable to get the profile, just use header info
							view.addAclEntry(new AclEntry(principalId, ra.getAccessType(), isOwner, header.getUserName(), true));
//							view.showErrorMessage(caught.getMessage());
						}
					});
				} else {
					AccessControlListEditor.getTeam(principalId, synapseClient, adapterFactory, new AsyncCallback<Team>() {
						@Override
						public void onSuccess(Team team) {
							view.addAclEntry(new AclEntry(principalId, ra.getAccessType(), isOwner, team));
						}
						@Override
						public void onFailure(Throwable caught) {
							view.addAclEntry(new AclEntry(principalId, ra.getAccessType(), isOwner, header.getUserName(), false));
//							view.showErrorMessage(caught.getMessage());
						}
					});
				}
				
				
			} else {
				showErrorMessage("Could not find user " + principalId);
			}
		}
	}

	public static void getUserProfile(String userId, SynapseClientAsync synapseClient, final AdapterFactory adapterFactory,  final AsyncCallback<UserProfile> callback){
		synapseClient.getUserProfile(userId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String userProfileJson) {
				try {
					callback.onSuccess(new UserProfile(adapterFactory.createNew(userProfileJson)));
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}    				
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public static void getTeam(String teamId, SynapseClientAsync synapseClient, final AdapterFactory adapterFactory, final AsyncCallback<Team> callback){
		synapseClient.getTeam(teamId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				try {
					callback.onSuccess(new Team(adapterFactory.createNew(json)));
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}    				
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	private void fetchUserGroupHeaders(final AsyncCallback<Void> callback) {
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
						callback.onSuccess(null);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
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
			unsavedChanges = true;
			setViewDetails();
		}
		unsavedChanges = true;
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
					fetchUserGroupHeaders(updateViewDetailsCallback());					
				} catch (Throwable e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					showErrorMessage("Unable to fetch benefactor permissions.");
			}
		});
	}
	
	@Override
	public void pushChangesToSynapse(final boolean recursive, final AsyncCallback<EntityWrapper> changesPushedCallback) {
		if(unsavedViewChanges) {
			view.alertUnsavedViewChanges(new Callback() {
				
				@Override
				public void invoke() {
					pushChangesToSynapse(recursive, changesPushedCallback);
				}
			});
			return;
		}
		
		// TODO: Make recursive option for "Create"
		if (!unsavedChanges) {			
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
				DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view);
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
			notifyNewUsers();
		} else if (hasLocalACL_inPortal && hasLocalACL_inRepo) {
			// Local ACL exists in both Portal and Repo
			// Apply updates to local ACL in Repo
			synapseClient.updateAcl(aclEW, recursive, callback);
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
				newPrincipalIdSet.add(ra.getPrincipalId().toString());
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
				String message = DisplayUtils.getShareMessage(entity.getId(), gwt.getHostPageBaseURL());
				String subject = entity.getName() + DisplayConstants.SHARED_ON_SYNAPSE_SUBJECT;
				synapseClient.sendMessage(newPrincipalIdSet, subject, message, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
					}
					@Override
					public void onFailure(Throwable caught) {
						if (!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)){
							view.showErrorMessage(caught.getMessage());
						}
					}
				});
			}
		}
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
	
}
