package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidget implements RestrictionWidgetView.Presenter, SynapseWidgetPresenter {
	
	com.google.gwt.core.client.Callback<Void, Throwable> entityUpdatedCallback;
	EntityBundle bundle;
	
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private JSONObjectAdapter jsonObjectAdapter;
	private JiraURLHelper jiraURLHelper;
	private GlobalApplicationState globalApplicationState;
	private RestrictionWidgetView view;
	private boolean showChangeLink, showIfProject, showFlagLink;
	
	@Inject
	public RestrictionWidget(
			RestrictionWidgetView view,
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalApplicationState,
			JiraURLHelper jiraURLHelper) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.globalApplicationState = globalApplicationState;
		this.jiraURLHelper = jiraURLHelper;
	}
	
	public void configure(EntityBundle bundle, boolean showChangeLink, boolean showIfProject, boolean showFlagLink, com.google.gwt.core.client.Callback<Void, Throwable> entityUpdatedCallback) {
		this.entityUpdatedCallback = entityUpdatedCallback;
		this.showChangeLink = showChangeLink;
		this.showIfProject = showIfProject;
		this.showFlagLink = showFlagLink;
		setEntity(bundle);
	}
	
	public void setEntity(EntityBundle bundle) {
		if(bundle == null)  throw new IllegalArgumentException("Entity is required");
		this.bundle = bundle;
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	public String getJiraFlagUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
		return jiraURLHelper.createFlagIssue(
				primaryEmail, 
				DisplayUtils.getDisplayName(userProfile), 
				bundle.getEntity().getId());
	}

	public String getJiraRestrictionUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
		return jiraURLHelper.createAccessRestrictionIssue(
				primaryEmail,
				DisplayUtils.getDisplayName(userProfile), 
				bundle.getEntity().getId());
	}

	public String getJiraRequestAccessUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
		return jiraURLHelper.createRequestAccessIssue(
				userProfile.getOwnerId(), 
				DisplayUtils.getDisplayName(userProfile), 
				primaryEmail, 
				bundle.getEntity().getId(), 
				getAccessRequirement().getId().toString());
	}
	
	public boolean hasAdministrativeAccess() {
		return bundle.getPermissions().getCanChangePermissions();
	}
	
	public RESTRICTION_LEVEL getRestrictionLevel() {
		return GovernanceServiceHelper.entityRestrictionLevel(bundle.getAccessRequirements());
	}

	public APPROVAL_TYPE getApprovalType() {
		return GovernanceServiceHelper.accessRequirementApprovalType(getAccessRequirement());
	}

	public boolean hasFulfilledAccessRequirements() {
		return bundle.getUnmetAccessRequirements().size()==0L;
	}

	public boolean includeRestrictionWidget() {
		return (bundle.getEntity() instanceof FileEntity) || (bundle.getEntity() instanceof Locationable) || (bundle.getEntity() instanceof Folder) || (showIfProject && bundle.getEntity() instanceof Project);
	}

	public String accessRequirementText() {
		return GovernanceServiceHelper.getAccessRequirementText(getAccessRequirement());
	}
	
	private AccessRequirement getAccessRequirement() {
		return GovernanceServiceHelper.selectAccessRequirement(bundle.getAccessRequirements(), bundle.getUnmetAccessRequirements());
	}

	public Callback accessRequirementCallback() {
		if (APPROVAL_TYPE.USER_AGREEMENT!=GovernanceServiceHelper.accessRequirementApprovalType(getAccessRequirement())) 
			throw new IllegalStateException("not a 'User Agreement' requirement type");
		return new Callback() {
			@Override
			public void invoke() {
				// create the self-signed access approval, then update this object
				String principalId = getUserProfile().getOwnerId();
				AccessRequirement ar = getAccessRequirement();
				Callback onSuccess = new Callback() {
					@Override
					public void invoke() {
						entityUpdatedCallback.onSuccess(null);
					}
				};
				CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
					@Override
					public void invoke(Throwable t) {
						entityUpdatedCallback.onFailure(t);
					}
				};
				GovernanceServiceHelper.signTermsOfUse(
						principalId, 
						ar.getId(), 
						onSuccess, 
						onFailure, 
						synapseClient, 
						jsonObjectAdapter);
			}
		};
	}

	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
		return (sessionData==null ? null : sessionData.getProfile());				
	}
	
	public boolean isAnonymous() {
		return getUserProfile()==null;
	}

	@Override
	public Widget asWidget() {
		if (!includeRestrictionWidget()) return null;
		boolean isAnonymous = isAnonymous();
		boolean hasAdministrativeAccess = false;
		boolean hasFulfilledAccessRequirements = false;
		String jiraFlagLink = null;
		if (!isAnonymous && showChangeLink) {
			hasAdministrativeAccess = hasAdministrativeAccess();
			jiraFlagLink = getJiraFlagUrl();
		}
		RESTRICTION_LEVEL restrictionLevel = getRestrictionLevel();
		APPROVAL_TYPE approvalType = getApprovalType();
		String accessRequirementText = null;
		Callback touAcceptanceCallback = null;
		Callback requestACTCallback = null;
		Callback imposeRestrictionsCallback = getImposeRestrictionsCallback();
		Callback loginCallback = getLoginCallback();
		if (approvalType!=APPROVAL_TYPE.NONE) {
			accessRequirementText = accessRequirementText();
			if (approvalType==APPROVAL_TYPE.USER_AGREEMENT) {
				touAcceptanceCallback = accessRequirementCallback();
			} else { // APPROVAL_TYPE.ACT_APPROVAL
				// get the Jira link for ACT approval
				if (!isAnonymous) {
					requestACTCallback = new Callback() {
						@Override
						public void invoke() {
							view.open(getJiraRequestAccessUrl());
						}
					};
				}
			}
			if (!isAnonymous) hasFulfilledAccessRequirements = hasFulfilledAccessRequirements();
		}
		return view.asWidget(jiraFlagLink, isAnonymous, hasAdministrativeAccess, accessRequirementText, touAcceptanceCallback, requestACTCallback, imposeRestrictionsCallback, loginCallback, restrictionLevel, approvalType, hasFulfilledAccessRequirements, showFlagLink);
	}
	
	public Callback getImposeRestrictionsCallback() {
		return new Callback() {
			@Override
			public void invoke() {
				synapseClient.createLockAccessRequirement(bundle.getEntity().getId(), new AsyncCallback<EntityWrapper>(){
					@Override
					public void onSuccess(EntityWrapper result) {
						entityUpdatedCallback.onSuccess(null);
					}
					@Override
					public void onFailure(Throwable caught) {
						entityUpdatedCallback.onFailure(caught);
					}
				});
			}
		};
	}
	
	public Callback getLoginCallback() {
		return new Callback() {
			public void invoke() {		
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		};
	}

}
