package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtilsGWT;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclPrincipal;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessControlListEditor implements AccessControlListEditorView.Presenter {
	
	private static final String LOCAL_ACL_CREATION_ERROR = "Creation of local sharing settings failed. Please try again.";
	private AccessControlListEditorView view;
	private NodeModelCreator nodeModelCreator;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter = null;
	
	private String entityId;
	private UserEntityPermissions uep;
	private AccessControlList acl;
	private Map<String, AclPrincipal> groupPrincipals;
	
	@Inject
	public AccessControlListEditor(AccessControlListEditorView view,
			SynapseClientAsync synapseClientAsync,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState, 
			JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.synapseClient = synapseClientAsync;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		view.setPresenter(this);
	}	
	
	public void setResource(Entity entity) {
		this.entityId=entity.getId();
	}
		
	public void setResource(String entityId) {
		this.entityId=entityId;
	}
	
	public Widget asWidget() {
		if (entityId==null) throw new IllegalStateException("Entity ID must be specified.");
		
		// Hide the view panel contents until async callback completes
		view.showLoading();
		
		int partsMask = EntityBundleTransport.ACL |
						EntityBundleTransport.PERMISSIONS |
						EntityBundleTransport.GROUPS;
		synapseClient.getEntityBundle(entityId, partsMask, new AsyncCallback<EntityBundleTransport>(){				
				@Override
				public void onSuccess(EntityBundleTransport bundle) {
					try {
						// deserialize ACL
						acl = nodeModelCreator.createEntity(bundle.getAclJson(), AccessControlList.class);
						// deserialize permissions info
						uep = nodeModelCreator.createEntity(bundle.getPermissionsJson(), UserEntityPermissions.class);
						// deserialize usersGroups and translate to ACLEntries
						PaginatedResults<UserGroup> userGroups = nodeModelCreator.createPaginatedResults(bundle.getGroupsJson(), UserGroup.class);
						groupPrincipals = getAclPrincipalsFromGroups(userGroups.getResults());
						setViewDetails();
					} catch (RestServiceException e) {
						onFailure(e);
					}
					
				}

				@Override
				public void onFailure(Throwable throwable) {
					throwable.printStackTrace();
					
					showErrorMessage(LOCAL_ACL_CREATION_ERROR);
				}
			});
			
		return view.asWidget();
	}
	
	public static Map<String, AclPrincipal> getAclPrincipalsFromGroups(Collection<UserGroup> groups) {
		Map<String, AclPrincipal> principals = new HashMap<String, AclPrincipal>();
		for (UserGroup g : groups) {
			AclPrincipal p = new AclPrincipal();
			p.setPrincipalId(Long.parseLong(g.getId()));
			p.setIndividual(false);
			p.setDisplayName(g.getName());
			p.setOwner(false);
			principals.put(g.getId(), p);
		}
		return principals;
	}
	
	private static AclPrincipal convertProfileToPrincipal(Long ownerPrincipalId, 
			UserProfile profile, ResourceAccess ra) {
		AclPrincipal p = new AclPrincipal();
		p.setDisplayName(profile.getDisplayName());
		p.setEmail(profile.getEmail());
		p.setIndividual(true);
		p.setPrincipalId(ra.getPrincipalId());
		p.setOwner(ownerPrincipalId.equals(ra.getPrincipalId()));
		if (profile.getPic() != null) 
			p.setPicUrl(DisplayUtils.createUserProfileAttachmentUrl(
				DisplayUtilsGWT.BASE_PROFILE_ATTACHMENT_URL, profile.getOwnerId(), 
				profile.getPic().getPreviewId(), null));
		return p;
	}

	private void setViewDetails() {
		if (this.entityId==null) throw new IllegalStateException("Entity must be specified.");
		if (this.acl==null) throw new IllegalStateException("ACL is missing.");
		if (this.uep==null) throw new IllegalStateException("User's entity permissions are missing.");		
		boolean isInherited = isInherited(acl, entityId);
		boolean canEnableInheritance = uep.getCanEnableInheritance();
		view.buildWindow(isInherited, canEnableInheritance);
		populateAclEntries(acl, uep.getOwnerPrincipalId());
	}
	
	// create a new ACL and push to Synapse
	@Override
	public void createAcl() {
		try {
			acl = newACLforEntity(this.entityId, uep.getOwnerPrincipalId());
		} catch (Exception e) {
			showErrorMessage(LOCAL_ACL_CREATION_ERROR);
			return;
		}
		createACLInSynapse();
	}
	
	@Override
	public void addAccess(Long principal, PermissionLevel permissionLevel) {
		if (principal.equals(uep.getOwnerPrincipalId()))
			showErrorMessage("Owner permissions cannot be modified. Please select a different user or group.");
		else
			setAccess(principal, permissionLevel);
	}
	
	@Override
	public void changeAccess(Long principal, PermissionLevel permissionLevel) {
		setAccess(principal, permissionLevel);
	}
	
	private void setAccess(Long principalId, PermissionLevel permissionLevel) {
		try {
			if (this.entityId==null) throw new IllegalStateException("Entity must be specified.");
			if (this.acl==null) throw new IllegalStateException("ACL must be specified.");
			ResourceAccess ra = findPrincipal(principalId, acl);
			if (ra==null) {
				ra = new ResourceAccess();
				ra.setPrincipalId(principalId);
				ra.setAccessType(AclUtils.getACCESS_TYPEs(permissionLevel));
				acl.getResourceAccess().add(ra);
			} else {
				ra.setAccessType(AclUtils.getACCESS_TYPEs(permissionLevel));
			}
		} catch (Exception e) {
			showErrorMessage(LOCAL_ACL_CREATION_ERROR);
			return;
		}
		updateACLInSynapse(false);
	}
	
	// clone the current ACL, copying over the entries but skipping the given one
	// "Remove failed. Please try again."
	@Override
	public void removeAccess(Long principalId) {
		try{
			if (this.entityId==null) throw new IllegalStateException("Entity must be specified.");
		
			if (this.acl==null) throw new IllegalStateException("ACL must be specified.");
			ResourceAccess ra = findPrincipal(principalId, acl);
			if (ra==null) {
				throw new IllegalStateException("ACL does not have a record for "+principalId);
			} else {
				acl.getResourceAccess().remove(ra);
			}
		} catch (Exception e) {
			showErrorMessage(LOCAL_ACL_CREATION_ERROR);
			return;
		}
		updateACLInSynapse(false);
	}


	@Override
	public void deleteAcl() {
		if (this.acl==null) throw new IllegalStateException("ACL must be specified.");
		synapseClient.deleteAcl(acl.getId(), new AsyncCallback<EntityWrapper>(){
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					acl = nodeModelCreator.createEntity(result, AccessControlList.class);
				} catch (RestServiceException e) {
					onFailure(e);
				}
				refresh(new VoidCallback(){
					@Override
					public void success() {
						// nothing more to do
					}
					@Override
					public void failure(Throwable t) {
						onFailure(t);
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showErrorMessage(LOCAL_ACL_CREATION_ERROR);
			}

		});
	}
	
	@Override
	public void applyAclToChildren() {
		try {
			if (this.entityId==null) throw new IllegalStateException("Entity must be specified.");
			if (this.acl==null) throw new IllegalStateException("ACL must be specified.");
		} catch (Exception e) {
			showErrorMessage(LOCAL_ACL_CREATION_ERROR);
			return;
		}
		updateACLInSynapse(true);
	}
	
	/*
	 * Private Methods
	 */
	
	private void showErrorMessage(String s) {
			view.showErrorMessage(s);
	}


	private void createACLInSynapse() {
		EntityWrapper aclEntityWrapper = null;
		try {
			JSONObjectAdapter aclJson = acl.writeToJSONObject(jsonObjectAdapter.createNew());
			aclEntityWrapper = new EntityWrapper(aclJson.toJSONString(), AccessControlList.class.getName(), null);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(LOCAL_ACL_CREATION_ERROR);
			return;
		}
		synapseClient.createAcl(aclEntityWrapper, new AsyncCallback<EntityWrapper>(){

			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					acl = nodeModelCreator.createEntity(result, AccessControlList.class);
				} catch (RestServiceException e) {
					onFailure(e);
				}
				refresh(new VoidCallback(){
					@Override
					public void success() {
						// nothing more to do
					}
					@Override
					public void failure(Throwable t) {
						onFailure(t);
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showErrorMessage(LOCAL_ACL_CREATION_ERROR);
			}
		});
	}
	
	
	private void updateACLInSynapse(boolean recursive) {
		EntityWrapper aclEntityWrapper = null;
		try {
			JSONObjectAdapter aclJson = acl.writeToJSONObject(jsonObjectAdapter.createNew());
			aclEntityWrapper = new EntityWrapper(aclJson.toJSONString(), aclJson.getClass().getName(), null);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(LOCAL_ACL_CREATION_ERROR);
		}
		synapseClient.updateAcl(aclEntityWrapper, recursive, new AsyncCallback<EntityWrapper>(){
	
				@Override
				public void onSuccess(EntityWrapper result) {
					try {
						acl = nodeModelCreator.createEntity(result, AccessControlList.class);
					} catch (RestServiceException e) {
						onFailure(e);
					}
					refresh(new VoidCallback(){
						@Override
						public void success() {
							// nothing more to do
						}
						@Override
						public void failure(Throwable t) {
							onFailure(t);
						}
					});
				}
				
				@Override
				public void onFailure(Throwable caught) {
					showErrorMessage(LOCAL_ACL_CREATION_ERROR);
				}
			});
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
				throw (RuntimeException)t; 
			else throw new RuntimeException(t);
		}
	}
	
	private void refresh(final VoidCallback callback) {
		if (this.entityId==null) throw new IllegalStateException("Entity must be specified.");
		view.showLoading();
		// get the updated ACL and UsersEntityPermissions
		int partsMask = EntityBundleTransport.ACL | EntityBundleTransport.PERMISSIONS;
		synapseClient.getEntityBundle(entityId, partsMask, new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport bundle) {
				try {
					// deserialize ACL
					acl = nodeModelCreator.createEntity(bundle.getAclJson(), AccessControlList.class);
					// deserialize permissions info
					uep = nodeModelCreator.createEntity(bundle.getPermissionsJson(), UserEntityPermissions.class);
					setViewDetails();
					callback.success();
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
	
	
	/*
	 * Utility Methods
	 */
	public static boolean isInherited(AccessControlList acl, String entityId) {
		return !acl.getId().equals(entityId);
	}
	
	public static AclEntry findAclEntry(Collection<AclEntry> entries, Long principalId) {
		for (AclEntry entry : entries) {
			if (entry.getPrincipal().getPrincipalId().equals(principalId)) return entry;
		}
		return null;
	}
	
	public void populateAclEntries(AccessControlList acl, final Long ownerPrincipalId) {
		for (final ResourceAccess ra : acl.getResourceAccess()) {
			String principalId = ra.getPrincipalId().toString();
			if (groupPrincipals.containsKey(principalId)) {
				// Principal is a group; use the cached AclPrincipal
				AclPrincipal p = groupPrincipals.get(principalId);	
				view.addAclEntry(new AclEntry(p, new ArrayList<ACCESS_TYPE>(ra.getAccessType())));
			} else {					
				// Principal is a user; fetch the UserProfile
				synapseClient.getUserProfile(principalId, new AsyncCallback<String>(){
					@Override
					public void onSuccess(String userProfileJson) {
						try {								
							UserProfile profile = nodeModelCreator.createEntity(userProfileJson, UserProfile.class);
							AclPrincipal p = convertProfileToPrincipal(ownerPrincipalId, profile, ra);
							view.addAclEntry(new AclEntry(p, new ArrayList<ACCESS_TYPE>(ra.getAccessType())));
						} catch (RestServiceException e) {
							onFailure(e);
						}
					}

					@Override
					public void onFailure(Throwable caught) {}
				});
			}
		}
	}
	
	public static AccessControlList newACLforEntity(String entityId, Long creatorPrincipalId) {
		if (entityId==null) throw new IllegalStateException("Entity must be specified.");
		if (creatorPrincipalId==null)  throw new IllegalStateException("Entity creator must be specified.");
		AccessControlList acl = new AccessControlList();
		acl.setId(entityId);
		Set<ResourceAccess> ras = new HashSet<ResourceAccess>();
		acl.setResourceAccess(ras);
		addOwnerAdministrativeAccess(acl, creatorPrincipalId);	
		return acl;
	}
	
	public static void addOwnerAdministrativeAccess(AccessControlList acl, Long creatorPrincipalId) {
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(creatorPrincipalId);
		Set<ACCESS_TYPE> ats = AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER);
		ra.setAccessType(ats);

		acl.getResourceAccess().add(ra);
	}

	/**
	 * Note this returns a **pointer** to the ResourceAccess record.  Therefore, changing
	 * the returned object also changes the parent ACL.
	 * @param principalId
	 * @param acl
	 * @return
	 */
	public static ResourceAccess findPrincipal(Long principalId, AccessControlList acl) {
		for (ResourceAccess ra : acl.getResourceAccess()) {
			if (ra.getPrincipalId().equals(principalId)) return ra;
		}
		return null;
	}
	

}
