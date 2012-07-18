package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	
	private AccessControlListEditorView view;
	private NodeModelCreator nodeModelCreator;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private JSONObjectAdapter jsonObjectAdapter = null;
	
	private String entityId;
	private UserEntityPermissions uep;
	private AccessControlList acl;
	private Map<String, AclPrincipal> allPrincipals;
	
	@Inject
	public AccessControlListEditor(AccessControlListEditorView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState, 
			JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
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

		int partsMask = EntityBundleTransport.ACL | 
			EntityBundleTransport.GROUPS |
			EntityBundleTransport.USERS |
			EntityBundleTransport.PERMISSIONS;
		synapseClient.getEntityBundle(entityId, partsMask, new AsyncCallback<EntityBundleTransport>(){
				@Override
				public void onSuccess(EntityBundleTransport bundle) {
					try {
						// deserialize ACL
						acl = nodeModelCreator.createEntity(bundle.getAclJson(), AccessControlList.class);
						// deserialize permissions info
						uep = nodeModelCreator.createEntity(bundle.getPermissionsJson(), UserEntityPermissions.class);
						// deserialize users and groups and translate to ACLEntries
						PaginatedResults<UserProfile> userProfiles = nodeModelCreator.createPaginatedResults(bundle.getUsersJson(), UserProfile.class);
						PaginatedResults<UserGroup> userGroups = nodeModelCreator.createPaginatedResults(bundle.getGroupsJson(), UserGroup.class);
						allPrincipals = getAclPrincipalsFromUsers(userProfiles.getResults(), uep.getOwnerPrincipalId().toString());
						allPrincipals.putAll(getAclPrincipalsFromGroups(userGroups.getResults()));	
						setViewDetails();
					} catch (RestServiceException e) {
						onFailure(e);
					}
					
				}

				@Override
				public void onFailure(Throwable throwable) {
					throwable.printStackTrace();
					
					showErrorMessage("Creation of local sharing settings failed. Please try again.");
				}
			});
			
		return view.asWidget();
	}	
	
	private void setViewDetails() {
		if (this.entityId==null) throw new IllegalStateException("Entity must be specified.");
		if (this.allPrincipals==null) throw new IllegalStateException("Principals list must be specified.");
		if (this.acl==null) throw new IllegalStateException("ACL is missing.");
		if (this.uep==null) throw new IllegalStateException("User's entity permissions are missing.");
		List<AclEntry> aclEntries = getAclEntries(acl, allPrincipals);
		boolean isInherited = isInherited(acl, entityId);
		boolean canEnableInheritance = uep.getCanEnableInheritance();
		// make sure the owner is in the collection
		boolean foundOwner = false;
		for (AclEntry entry : aclEntries) {
			if (entry.getPrincipal().isOwner()) {
				foundOwner=true; 
				break;
			}
		}
		if (!foundOwner) {
			AclEntry ownerEntry = new AclEntry();
			AclPrincipal p = allPrincipals.get(uep.getOwnerPrincipalId().toString());
			p.setOwner(true);
			ownerEntry.setPrincipal(p);
			ownerEntry.setAccessTypes(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER));
			aclEntries.add(ownerEntry);
		}
		
		// create an ordered list of the 'universe' of principals who can be added to the ACL
		List<AclPrincipal> orderedPrincipals = new ArrayList<AclPrincipal>();
		// first add the groups
		for (AclPrincipal p : allPrincipals.values()) {
			if (!p.isIndividual()) orderedPrincipals.add(p);
		}
		// now add the individuals (but skip the owner, who can't be added to or omitted from the ACL)
		for (AclPrincipal p : allPrincipals.values()) {
			if (p.isIndividual() && !p.isOwner()) orderedPrincipals.add(p);
		}
		
		view.setAclDetails(aclEntries, orderedPrincipals, isInherited, canEnableInheritance);		
	}
	
	// create a new ACL and push to Synapse
	@Override
	public void createAcl() {
		try {
			acl = newACLforEntity(this.entityId, uep.getOwnerPrincipalId());
		} catch (Exception e) {
			showErrorMessage("Creation of local sharing settings failed. Please try again.");
			return;
		}
		createACLInSynapse();
	}
	
	@Override
	public void addAccess(Long principal, PermissionLevel permissionLevel) {
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
				ra.setAccessType(new HashSet<ACCESS_TYPE>(AclUtils.getACCESS_TYPEs(permissionLevel)));
				acl.getResourceAccess().add(ra);
			} else {
				ra.setAccessType(new HashSet<ACCESS_TYPE>(AclUtils.getACCESS_TYPEs(permissionLevel)));
			}
		} catch (Exception e) {
			showErrorMessage("Creation of local sharing settings failed. Please try again.");
			return;
		}
		updateACLInSynapse();
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
			showErrorMessage("Creation of local sharing settings failed. Please try again.");
			return;
		}
		updateACLInSynapse();
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
				showErrorMessage("Creation of local sharing settings failed. Please try again.");
			}

		});
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
			aclEntityWrapper = new EntityWrapper(aclJson.toJSONString(), aclJson.getClass().getName(), null);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage("Creation of local sharing settings failed. Please try again.");
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
				showErrorMessage("Creation of local sharing settings failed. Please try again.");
			}
		});
	}
	
	
	private void updateACLInSynapse() {
		EntityWrapper aclEntityWrapper = null;
		try {
			JSONObjectAdapter aclJson = acl.writeToJSONObject(jsonObjectAdapter.createNew());
			aclEntityWrapper = new EntityWrapper(aclJson.toJSONString(), aclJson.getClass().getName(), null);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage("Creation of local sharing settings failed. Please try again.");
		}
		synapseClient.updateAcl(aclEntityWrapper, new AsyncCallback<EntityWrapper>(){
	
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
					showErrorMessage("Creation of local sharing settings failed. Please try again.");
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
	
	public static Map<String, AclPrincipal> getAclPrincipalsFromUsers(Collection<UserProfile> users, String ownerId) {
		Map<String, AclPrincipal> principals = new HashMap<String, AclPrincipal>();
		for (UserProfile up : users) {
			AclPrincipal p = new AclPrincipal();
			p.setPrincipalId(Long.parseLong(up.getOwnerId()));
			p.setIndividual(true);
			p.setDisplayName(up.getDisplayName());
			p.setOwner(ownerId.equals(up.getOwnerId()));
			principals.put(up.getOwnerId(), p);
		}
		return principals;
	}
	
	public static AclEntry findAclEntry(Collection<AclEntry> entries, Long principalId) {
		for (AclEntry entry : entries) {
			if (entry.getPrincipal().getPrincipalId().equals(principalId)) return entry;
		}
		return null;
	}
	
	public static List<AclEntry> getAclEntries(AccessControlList acl, Map<String, AclPrincipal> universe) {
		List<AclEntry> entries = new ArrayList<AclEntry>();
		for (ResourceAccess ra : acl.getResourceAccess()) {
			// is the principal already in the entities?
			AclEntry entry = findAclEntry(entries, ra.getPrincipalId());
			if (entry==null) {
				entry = new AclEntry();
				String principalIdString = ra.getPrincipalId().toString();
				if (universe.containsKey(principalIdString)) {
					entry.setPrincipal(universe.get(principalIdString));
				} else {
					throw new IllegalArgumentException(
							"Access Control List has a principal not represented in the given Users or Groups.");
				}
				entry.setAccessTypes(new ArrayList<ACCESS_TYPE>(ra.getAccessType()));
				entries.add(entry);
			} else {
				entry.getAccessTypes().addAll(ra.getAccessType());
			}
		}
		return entries;
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
		Set<ACCESS_TYPE> ats = new HashSet<ACCESS_TYPE>();
		ats.addAll(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER));
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
