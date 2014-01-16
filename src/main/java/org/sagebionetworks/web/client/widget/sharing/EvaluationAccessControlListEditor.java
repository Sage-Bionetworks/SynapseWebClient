package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
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
 * Editor dialog to view and modify the Access Control List of a given Evaluation.
 */
public class EvaluationAccessControlListEditor implements EvaluationAccessControlListEditorView.Presenter {
	
	private static final String ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS = "Current user permissions cannot be modified. Please select a different user.";
	private static final String NULL_UEP_MESSAGE = "User's evaluation permissions are missing.";
	private static final String NULL_ACL_MESSAGE = "ACL is missing.";
	private static final String NULL_EVALUATION_MESSAGE = "Evaluation is missing.";
	
	// Editor components
	private EvaluationAccessControlListEditorView view;
	private NodeModelCreator nodeModelCreator;
	private SynapseClientAsync synapseClient;
	private UserAccountServiceAsync userAccountService;
	private JSONObjectAdapter jsonObjectAdapter;
	private AuthenticationController authenticationController;
	private boolean unsavedChanges;
	private boolean unsavedViewChanges;
	private PublicPrincipalIds publicPrincipalIds = null;
	GlobalApplicationState globalApplicationState;
	private AdapterFactory adapterFactory;
	
	// Entity components
	private Evaluation evaluation;
	private UserEvaluationPermissions uep;
	private AccessControlList acl;	
	private Map<String, UserGroupHeader> userGroupHeaders;
	
	@Inject
	public EvaluationAccessControlListEditor(EvaluationAccessControlListEditorView view,
			SynapseClientAsync synapseClientAsync,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,
			UserAccountServiceAsync userAccountService,
			GlobalApplicationState globalApplicationState,
			AdapterFactory adapterFactory) {
		this.view = view;
		this.synapseClient = synapseClientAsync;
		this.userAccountService = userAccountService;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.adapterFactory = adapterFactory;
		userGroupHeaders = new HashMap<String, UserGroupHeader>();
		view.setPresenter(this);		
	}	
	
	/**
	 * Set the entity with which this ACLEditor is associated.
	 */
	public void setResource(Evaluation evaluation) {
		if (!evaluation.equals(this.evaluation)) {
			acl = null;
			uep = null;
		}
		this.evaluation = evaluation;
	}
	
	/**
	 * Get the ID of the entity with which this ACLEditor is associated.
	 */
	public String getResourceId() {
		return evaluation == null ? null : evaluation.getId();
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
		view.setPublicPrincipalIds(publicPrincipalIds);
	}
	
	/**
	 * Refresh the ACLEditor by fetching from Synapse
	 */
	private void refresh(final AsyncCallback<Void> callback) {
		if (this.evaluation.getId() == null) throw new IllegalStateException(NULL_EVALUATION_MESSAGE);
		view.showLoading();
		if (publicPrincipalIds == null){
			userAccountService.getPublicAndAuthenticatedGroupPrincipalIds(new AsyncCallback<PublicPrincipalIds>() {
				@Override
				public void onSuccess(PublicPrincipalIds result) {
					if (result != null) {
						publicPrincipalIds = result;
						initViewPrincipalIds();
					}
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
			
		unsavedChanges = false;
		final Callback aclSetCallback = new Callback(){
			@Override
			public void invoke() {
				//once we have the acl and uep, fetch the user group headers
				fetchUserGroupHeaders();					
			}
		};
		
		Callback userPermissionsSetCallback = new Callback(){
			@Override
			public void invoke() {
				//now have user permissions.  now get the acl
				getEvaluationACL(aclSetCallback);
			}
		};
		
		getUserPermissions(userPermissionsSetCallback);
	}
	
	public void getUserPermissions(final Callback callback){
		synapseClient.getUserEvaluationPermissions(evaluation.getId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				//set the user permissions object
				try {
					uep = nodeModelCreator.createJSONEntity(result, UserEvaluationPermissions.class);
					callback.invoke();
				} catch (JSONObjectAdapterException e) {
					DisplayUtils.handleJSONAdapterException(e, globalApplicationState.getPlaceChanger(), authenticationController.getCurrentUserSessionData());
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	public void getEvaluationACL(final Callback callback){
		synapseClient.getEvaluationAcl(evaluation.getId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				//set the evaluation acl
				try {
					acl = nodeModelCreator.createJSONEntity(result, AccessControlList.class);
					callback.invoke();
				} catch (JSONObjectAdapterException e) {
					DisplayUtils.handleJSONAdapterException(e, globalApplicationState.getPlaceChanger(), authenticationController.getCurrentUserSessionData());
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	/**
	 * Send ACL details to the View.
	 */
	private void setViewDetails() {
		validateEditorState();
		view.showLoading();
		view.buildWindow(unsavedChanges);
		populateAclEntries();
	}
	
	private void populateAclEntries() {
		view.setIsOpenParticipation(false);
		for (final ResourceAccess ra : acl.getResourceAccess()) {
			Long pricipalIdLong = ra.getPrincipalId();
			final String principalId = ra.getPrincipalId().toString();
			final UserGroupHeader header = userGroupHeaders.get(principalId);
			final boolean isOwner = (ra.getPrincipalId().equals(uep.getOwnerPrincipalId()));
			if (header != null) {
				String title = header.getIsIndividual() ? DisplayUtils.getDisplayName(header.getFirstName(), header.getLastName(), header.getUserName()) : 
					header.getUserName();
				view.addAclEntry(new AclEntry(principalId, ra.getAccessType(), isOwner, title, "", header.getIsIndividual()));

				if (pricipalIdLong.equals(publicPrincipalIds.getAuthenticatedAclPrincipalId())) {
					PermissionLevel level = AclUtils.getPermissionLevel(ra.getAccessType());
					view.setIsOpenParticipation(PermissionLevel.CAN_PARTICIPATE_EVALUATION.equals(level));
				}
			} else {
				showErrorMessage("Could not find user " + principalId);
			}
		}
	}

	private void fetchUserGroupHeaders() {
		List<String> ids = new ArrayList<String>();
		for (ResourceAccess ra : acl.getResourceAccess())
			ids.add(ra.getPrincipalId().toString());
		if (ids.contains(uep.getOwnerPrincipalId().toString())) {
			ids.add(uep.getOwnerPrincipalId().toString());
		}
		synapseClient.getUserGroupHeadersById(ids, new AsyncCallback<EntityWrapper>(){
			@Override
			public void onSuccess(EntityWrapper wrapper) {
				try {	
					UserGroupHeaderResponsePage response = nodeModelCreator.createJSONEntity(wrapper.getEntityJson(), UserGroupHeaderResponsePage.class);
					for (UserGroupHeader ugh : response.getChildren())
						userGroupHeaders.put(ugh.getOwnerId(), ugh);
					setViewDetails();
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				setViewDetails();
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
			fetchUserGroupHeaders();
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
	public void pushChangesToSynapse(final AsyncCallback<Void> changesPushedCallback) {
		if(unsavedViewChanges) {
			view.alertUnsavedViewChanges(new Callback() {				
				@Override
				public void invoke() {
					pushChangesToSynapse(changesPushedCallback);
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
		String updatedAclJson;
		try {
			JSONObjectAdapter aclJson = acl.writeToJSONObject(jsonObjectAdapter.createNew());
			updatedAclJson = aclJson.toJSONString();
		} catch (JSONObjectAdapterException e) {
			DisplayUtils.handleJSONAdapterException(e, globalApplicationState.getPlaceChanger(), authenticationController.getCurrentUserSessionData());
			return;
		}
		
		// Create an async callback to receive the updated ACL from Synapse
		AsyncCallback<String> callback = new AsyncCallback<String>(){
			@Override
			public void onSuccess(String result) {
				try {
					acl = nodeModelCreator.createJSONEntity(result, AccessControlList.class);
					unsavedChanges = false;					
					setViewDetails();
					view.showInfoSuccess("Success", "Permissions were successfully saved to Synapse");
					changesPushedCallback.onSuccess(null);
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
		synapseClient.updateEvaluationAcl(updatedAclJson, callback);				
	}
	
	/**
	 * @return the uep associated with the entity
	 */
	public UserEvaluationPermissions getUserEntityPermissions() {
		return uep;
	}
	
	private String getCurrentUserId() {
		return authenticationController.getCurrentUserPrincipalId();
	}

	/**
	 * Ensure the editor has a valid Entity ID, ACL, and User Permissions.
	 */
	private void validateEditorState() {
		if (this.evaluation.getId() == null) throw new IllegalStateException(NULL_EVALUATION_MESSAGE);
		if (this.acl == null) throw new IllegalStateException(NULL_ACL_MESSAGE);
		if (this.uep == null) throw new IllegalStateException(NULL_UEP_MESSAGE);
	}
	
	private void showErrorMessage(String s) {
		view.showErrorMessage(s);
	}
}
