package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessRequirementDialog implements AccessRequirementDialogView.Presenter, SynapseWidgetPresenter {
	
	com.google.gwt.core.client.Callback<Void, Throwable> entityUpdatedCallback;
	
	private AccessRequirement ar;
	private AccessRequirementDialogView view;
	AuthenticationController authenticationController;
	GlobalApplicationState globalApplicationState;
	SynapseClientAsync synapseClient;
	JSONObjectAdapter jsonObjectAdapter;
	JiraURLHelper jiraURLHelper;
	String entityId;
	Callback imposeRestrictionCallback, finishedCallback;
	
	@Inject
	public AccessRequirementDialog(
			AccessRequirementDialogView view,
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalApplicationState,
			JiraURLHelper jiraURLHelper,
			IconsImageBundle iconsImageBundle) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.jiraURLHelper = jiraURLHelper;
		view.setPresenter(this);
	}
	
	public void configure(
			AccessRequirement ar,
			String entityId,
			boolean hasAdministrativeAccess,
			boolean hasFulfilledAccessRequirements,
			Callback imposeRestrictionsCallback, 
			Callback finishedCallback) {
		this.ar = ar;
		this.entityId = entityId;
		this.imposeRestrictionCallback = imposeRestrictionsCallback;
		this.finishedCallback = finishedCallback;
		boolean isAnonymous = isAnonymous();
		APPROVAL_TYPE approvalType = getApprovalType();
		RESTRICTION_LEVEL restrictionLevel = getRestrictionLevel();
		
		if ((restrictionLevel==RESTRICTION_LEVEL.OPEN && approvalType!=APPROVAL_TYPE.NONE) ||
				(restrictionLevel!=RESTRICTION_LEVEL.OPEN && approvalType==APPROVAL_TYPE.NONE)) 
			throw new IllegalArgumentException("restrictionLevel="+restrictionLevel+" but approvalType="+approvalType);
		if (restrictionLevel!=RESTRICTION_LEVEL.OPEN && isAnonymous && hasFulfilledAccessRequirements) 
			throw new IllegalArgumentException("restrictionLevel!=APPROVAL_REQUIRED.NONE && isAnonymous && accessApproved");
		boolean imposeRestrictionsAllowed = (restrictionLevel==RESTRICTION_LEVEL.OPEN && hasAdministrativeAccess);
		
		if (approvalType!=APPROVAL_TYPE.NONE) {
			String arText = GovernanceServiceHelper.getAccessRequirementText(ar); 
			if (approvalType==APPROVAL_TYPE.USER_AGREEMENT) {
				//show user agreement that user can sign
				//view.show...
				//touAcceptanceCallback would have been set, should call back to signTermsOfUseClicked
			} else { // APPROVAL_TYPE.ACT_APPROVAL
				// get the Jira link for ACT approval
				if (!isAnonymous) {
					//show ACT request UI
					//view.show...
					//requestACTCallback would have been set, should call back to requestACTClicked
				}
			}
		}
		
	}
	
	public void show() {
		view.showModal();
	}
	
	public String getJiraFlagUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
		return jiraURLHelper.createFlagIssue(
				primaryEmail, 
				DisplayUtils.getDisplayName(userProfile), 
				entityId);
	}
	
	public String getJiraRequestAccessUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
		return jiraURLHelper.createRequestAccessIssue(
				userProfile.getOwnerId(), 
				DisplayUtils.getDisplayName(userProfile), 
				primaryEmail, 
				entityId, 
				ar.getId().toString());
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}
	
	public RESTRICTION_LEVEL getRestrictionLevel() {
		return GovernanceServiceHelper.getRestrictionLevel(ar);
	}

	public APPROVAL_TYPE getApprovalType() {
		return GovernanceServiceHelper.accessRequirementApprovalType(ar);
	}

	@Override
	public void imposeRestrictionClicked() {
		imposeRestrictionCallback.invoke();	
	}
	
	@Override
	public void signTermsOfUseClicked() {
		if (APPROVAL_TYPE.USER_AGREEMENT!=GovernanceServiceHelper.accessRequirementApprovalType(ar)) 
			throw new IllegalStateException("not a 'User Agreement' requirement type");
		// create the self-signed access approval, then update this object
		String principalId = getUserProfile().getOwnerId();
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
				ar, 
				onSuccess, 
				onFailure, 
				synapseClient, 
				jsonObjectAdapter);
	}

	@Override
	public void anonymousOkClicked() {
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
	}

	
	@Override
	public void requestACTClicked(){
		view.open(getJiraRequestAccessUrl());
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
		return view.asWidget();
	}
	
	
}
