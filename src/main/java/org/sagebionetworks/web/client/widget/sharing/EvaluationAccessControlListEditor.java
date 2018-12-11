package org.sagebionetworks.web.client.widget.sharing;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.ArrayList;
import java.util.Date;
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
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Editor dialog to view and modify the Access Control List of a given Evaluation.
 * This class is the Presenter in the MVP design pattern.
 * 
 * @author jay
 */
public class EvaluationAccessControlListEditor implements AccessControlListEditorView.Presenter {
	
	private static final String ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS = "Current user permissions cannot be modified. Please select a different user.";
	private static final String NULL_UEP_MESSAGE = "User's evaluation permissions are missing.";
	private static final String NULL_ACL_MESSAGE = "ACL is missing.";
	private static final String NULL_EVALUATION_MESSAGE = "Evaluation is missing.";
	
	// Editor components
	private AccessControlListEditorView view;
	private SynapseJavascriptClient jsClient;
	private ChallengeClientAsync challengeClient;
	private AuthenticationController authenticationController;
	private JSONObjectAdapter jsonObjectAdapter;
	
	PublicPrincipalIds publicPrincipalIds;
	
	// Evaluation components
	private Evaluation evaluation;
	private UserEvaluationPermissions uep;
	private AccessControlList acl;	
	private Map<String, UserGroupHeader> userGroupHeaders;
	private Set<String> originalPrincipalIdSet;
	HasChangesHandler hasChangesHandler;
	SynapseAlert synAlert;
	
	@Inject
	public EvaluationAccessControlListEditor(AccessControlListEditorView view,
			SynapseJavascriptClient jsClient,
			AuthenticationController authenticationController,
			SynapseProperties synapseProperties,
			JSONObjectAdapter jsonObjectAdapter,
			ChallengeClientAsync challengeClient,
			SynapseAlert synAlert
			) {
		this.view = view;
		this.jsClient = jsClient;
		this.challengeClient = challengeClient;
		fixServiceEntryPoint(challengeClient);
		this.authenticationController = authenticationController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.synAlert = synAlert;
		userGroupHeaders = new HashMap<String, UserGroupHeader>();
		view.setPresenter(this);
		view.setSynAlert(synAlert);
		view.setPermissionsToDisplay(getPermList(), getPermissionsToDisplay());
		view.setNotifyCheckboxVisible(false);
		view.setDeleteLocalACLButtonVisible(false);
		publicPrincipalIds = synapseProperties.getPublicPrincipalIds();
		initViewPrincipalIds();
	}

	
	/**
	 * Configure this widget before using it.
	 * Set the entity with which this ACLEditor is associated.
	 */
	public void configure(Evaluation evaluation, HasChangesHandler hasChangesHandler) {
		if (!evaluation.equals(this.evaluation)) {
			acl = null;
			uep = null;
		}
		this.hasChangesHandler = hasChangesHandler;
		this.evaluation = evaluation;
	}
	
	/**
	 * Get the ID of the entity with which this ACLEditor is associated.
	 */
	public String getResourceId() {
		return evaluation == null ? null : evaluation.getId();
	}
	
	public PermissionLevel[] getPermList() {
		return new PermissionLevel[] {PermissionLevel.CAN_VIEW, PermissionLevel.CAN_SUBMIT_EVALUATION, PermissionLevel.CAN_SCORE_EVALUATION, PermissionLevel.CAN_ADMINISTER_EVALUATION};
	}

	public HashMap<PermissionLevel, String> getPermissionsToDisplay() {
		HashMap<PermissionLevel, String> permissionDisplay = new HashMap<PermissionLevel, String>();
		permissionDisplay.put(PermissionLevel.CAN_VIEW, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_VIEW);
		permissionDisplay.put(PermissionLevel.CAN_SCORE_EVALUATION, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_SCORE);
		permissionDisplay.put(PermissionLevel.CAN_SUBMIT_EVALUATION, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_SUBMIT);
		permissionDisplay.put(PermissionLevel.CAN_ADMINISTER_EVALUATION, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_ADMINISTER);		
		return permissionDisplay;
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
	
	/**
	 * Refresh the ACLEditor by fetching from Synapse
	 */
	private void refresh(final AsyncCallback<Void> callback) {
		if (this.evaluation.getId() == null) throw new IllegalStateException(NULL_EVALUATION_MESSAGE);
		view.showLoading();
		hasChangesHandler.hasChanges(false);
		
		challengeClient.getEvaluationAcl(evaluation.getId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				//set the evaluation acl
				try {
					acl = new AccessControlList(jsonObjectAdapter.createNew(result));
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
					return;
				}
				
				//initialize original principal id set
				originalPrincipalIdSet = new HashSet<String>();
				for (final ResourceAccess ra : acl.getResourceAccess()) {
					final String principalId = ra.getPrincipalId().toString();
					originalPrincipalIdSet.add(principalId);
				}
				//default notification to true
				view.setIsNotifyPeople(true);
				getUserPermissions(callback);				
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	public void getUserPermissions(final AsyncCallback<Void> callback) {
		challengeClient.getUserEvaluationPermissions(evaluation.getId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				//set the user permissions object
				try {
					uep = new UserEvaluationPermissions(jsonObjectAdapter.createNew(result));
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
					return;
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
		view.buildWindow(false, false, null, false, true, PermissionLevel.CAN_VIEW, authenticationController.isLoggedIn());
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
		synAlert.clear();
		for (final ResourceAccess ra : acl.getResourceAccess()) {
			final String principalId = ra.getPrincipalId().toString();
			final UserGroupHeader header = userGroupHeaders.get(principalId);
			if (header != null) {
				String title = header.getIsIndividual() ? DisplayUtils.getDisplayName(header.getFirstName(), header.getLastName(), header.getUserName()) : 
					header.getUserName();
				view.addAclEntry(new AclEntry(principalId, ra.getAccessType(), title, "", header.getIsIndividual()));
			} else {
				synAlert.showError("Could not find user " + principalId);
			}
		}
	}
	
	private void fetchUserGroupHeaders(final AsyncCallback<Void> callback) {
		ArrayList<String> ids = new ArrayList<String>();
		for (ResourceAccess ra : acl.getResourceAccess())
			ids.add(ra.getPrincipalId().toString());
		jsClient.getUserGroupHeadersById(ids, new AsyncCallback<UserGroupHeaderResponsePage>(){
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
		synAlert.clear();
		if (currentUserId != null && principalId.toString().equals(currentUserId)) {
			synAlert.showError(ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS);
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
		synAlert.clear();
		if (currentUserId != null && principalIdToRemove.toString().equals(currentUserId)) {
			synAlert.showError(ERROR_CANNOT_MODIFY_ACTIVE_USER_PERMISSIONS);
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
			synAlert.showError("ACL does not have a record for " + principalIdToRemove);
		}
	}

	@Override
	public void createAcl() {
		validateEditorState();
		synAlert.clear();
		if (acl.getId().equals(evaluation.getId())) {
			synAlert.showError("Entity already has an ACL!");
			return;
		}		
		acl.setId(evaluation.getId());
		acl.setCreationDate(new Date());		
		hasChangesHandler.hasChanges(true);
		setViewDetails();
	}
	
	@Override
	public void deleteAcl() {
	}
	
	public void pushChangesToSynapse(final Callback changesPushedCallback) {
		validateEditorState();
		synAlert.clear();
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
				synAlert.handleException(caught);
				hasChangesHandler.hasChanges(true);
			}
		};
		
		applyChanges(callback);
	}
	
	protected void applyChanges(AsyncCallback<AccessControlList> callback) {
		// Apply changes
		challengeClient.updateEvaluationAcl(acl, callback);
	}
	
	public String getDisplayName(String principalId) {
		//get the user group header for this resource
		UserGroupHeader header = userGroupHeaders.get(principalId);
		if (header != null) {
			return DisplayUtils.getDisplayName(header);
		}
		return "(Unknown user)";
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

	public void refresh() {
		synAlert.clear();
		refresh(new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.showError(DisplayConstants.ERROR_ACL_RETRIEVAL_FAILED);
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
	
	public UserEvaluationPermissions getUserEvaluationPermissions() {
		return uep;
	}
	
}
