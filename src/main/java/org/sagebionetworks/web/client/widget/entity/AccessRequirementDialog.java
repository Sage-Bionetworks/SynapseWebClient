package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessRequirementDialog implements AccessRequirementDialogView.Presenter, SynapseWidgetPresenter {
	
	private AccessRequirement ar;
	private AccessRequirementDialogView view;
	AuthenticationController authenticationController;
	GlobalApplicationState globalApplicationState;
	SynapseClientAsync synapseClient;
	JSONObjectAdapter jsonObjectAdapter;
	JiraURLHelper jiraURLHelper;
	String entityId;
	Callback finishedCallback;
	Callback entityUpdated;
	WikiPageWidget wikiPageWidget;
	
	@Inject
	public AccessRequirementDialog(
			AccessRequirementDialogView view,
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalApplicationState,
			JiraURLHelper jiraURLHelper,
			WikiPageWidget wikiPageWidget
			) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.jiraURLHelper = jiraURLHelper;
		this.wikiPageWidget = wikiPageWidget;
		wikiPageWidget.showCreatedBy(false);
		wikiPageWidget.showModifiedBy(false);
		wikiPageWidget.showWikiHistory(false);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
	}
	
	public void configure(
			AccessRequirement ar,
			String entityId,
			boolean hasAdministrativeAccess,
			boolean accessApproved,
			Callback entityUpdated,
			Callback finishedCallback) {
		this.ar = ar;
		this.entityId = entityId;
		this.entityUpdated = entityUpdated;
		this.finishedCallback = finishedCallback;
		//hide all
		view.clear();
		boolean isAnonymous = isAnonymous();
		APPROVAL_TYPE approvalType = getApprovalType();
		RESTRICTION_LEVEL restrictionLevel = getRestrictionLevel();
		
		if ((restrictionLevel==RESTRICTION_LEVEL.OPEN && approvalType!=APPROVAL_TYPE.NONE) ||
				(restrictionLevel!=RESTRICTION_LEVEL.OPEN && approvalType==APPROVAL_TYPE.NONE)) 
			throw new IllegalArgumentException("restrictionLevel="+restrictionLevel+" but approvalType="+approvalType);
		if (restrictionLevel!=RESTRICTION_LEVEL.OPEN && isAnonymous && accessApproved) 
			throw new IllegalArgumentException("restrictionLevel!=APPROVAL_REQUIRED.NONE && isAnonymous && accessApproved");
		boolean imposeRestrictionsAllowed = (restrictionLevel==RESTRICTION_LEVEL.OPEN && hasAdministrativeAccess);
		
		if (restrictionLevel==RESTRICTION_LEVEL.OPEN) {
			view.showNoRestrictionsUI();
			view.showOpenUI();
		}
		else {
			view.showControlledUseUI();
			// next, if you are approved, comes "You have access to this data under the following..."
    		// or if you are not approved, "In order to Access..."
     		if (accessApproved) {
     			view.showApprovedHeading();
      		} else {
      			if (approvalType==APPROVAL_TYPE.USER_AGREEMENT) {
          			view.showTouHeading();
     			} else if (approvalType==APPROVAL_TYPE.ACT_APPROVAL) { //restrictionLevel==APPROVAL_REQUIRED.ACT_APPROVAL
          			view.showActHeading();
     			} else {
     				throw new IllegalArgumentException("Cannot have non-RESTRICTION_LEVEL.OPEN with APPROVAL_TYPE none.");
     			}
      		}
    		// next comes the Terms of Use or ACT info, in its own box
     		String terms = GovernanceServiceHelper.getAccessRequirementText(ar);
     		if (!DisplayUtils.isDefined(terms)) {
     			//get wiki terms
     			WikiPageKey wikiKey = new WikiPageKey(ar.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), null);
     			wikiPageWidget.configure(wikiKey, false, null, false);
     			view.showWikiTermsUI();
     		} else {
     			view.setTerms(terms);
     			view.showTermsUI();
     		}
     		
     		
     		// if not logged in there's an extra line "Note:  You must log in to access restricted data."
           	if (isAnonymous) {
           		view.showAnonymousAccessNote();
           	}
		}
		
		// next there's a prompt with a link to the Governance page
       	if (imposeRestrictionsAllowed) {
        	view.showImposeRestrictionsAllowedNote();
     	} else {
     		view.showImposeRestrictionsNotAllowedNote();
     	}
      	
		// finally there the Flag notice and hyperlink
      	// (but not for a user having admin access to their own dataaset
      	if (isAnonymous) {
      		view.showAnonymousFlagNote();
      	} else if (!imposeRestrictionsAllowed) {
      		view.showImposeRestrictionsNotAllowedFlagNote();
      	}
      	
      	// buttons
     	if (isAnonymous) {
      		// login or cancel
     		view.showLoginButton();
     		view.showCancelButton();
     	} else { 
      		if (approvalType==APPROVAL_TYPE.NONE) {
      			if (hasAdministrativeAccess) {
      				// button to add restriction or cancel
      				view.showImposeRestrictionsButton();
      				view.showCancelButton();
      			} else {
        			// just a close button
      				view.showCloseButton();
     			}
    		} else {
     			if (accessApproved) {
     				// just a close button
     				view.showCloseButton();
     			} else {
	     			if (approvalType==APPROVAL_TYPE.USER_AGREEMENT) {
	     				// agree to TOU, cancel
	     				view.showSignTermsButton();
	     				view.showCancelButton();
	     			} else { // APPROVAL_TYPE.ACT_APPROVAL
	     				if (isShowRequestAccessFromACTButton()) {
	     					// request access, cancel
	     					view.showRequestAccessFromACTButton();
		     				view.showCancelButton();	
	     				} else {
	     					view.showCloseButton();
	     				}
	     			}
     			}
      		}
      	}
		
	}
	
	public void show() {
		view.showModal();
	}
	
	public void hide() {
		view.hideModal();
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
	
	public RESTRICTION_LEVEL getRestrictionLevel() {
		return GovernanceServiceHelper.getRestrictionLevel(ar);
	}

	public APPROVAL_TYPE getApprovalType() {
		return GovernanceServiceHelper.accessRequirementApprovalType(ar);
	}
	
	public boolean isShowRequestAccessFromACTButton() {
		if (ar instanceof ACTAccessRequirement) {
			Boolean openJiraIssue = ((ACTAccessRequirement)ar).getOpenJiraIssue();
			return openJiraIssue == null || openJiraIssue.booleanValue();
		}
		return false;
	}


	@Override
	public void imposeRestrictionClicked() {
		view.hideModal();
		imposeRestriction(entityId, entityUpdated);
	}
	
	public void imposeRestriction(String entityId, final Callback entityUpdated) {
		view.hideModal();
		synapseClient.createLockAccessRequirement(entityId, new AsyncCallback<ACTAccessRequirement>(){
			@Override
			public void onSuccess(ACTAccessRequirement result) {
				if (entityUpdated != null)
					entityUpdated.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
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
				finished();
			}
		};
		CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
			@Override
			public void invoke(Throwable t) {
				view.showErrorMessage(t.getMessage());
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

	public void finished() {
		if (finishedCallback != null)
			finishedCallback.invoke();
	}
	
	@Override
	public void flagClicked(){
		view.hideModal();
		view.open(getJiraFlagUrl());
	}

	@Override
	public void requestACTClicked(){
		view.hideModal();
		view.open(getJiraRequestAccessUrl());
	}
	
	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
		return (sessionData==null ? null : sessionData.getProfile());				
	}
	
	@Override
	public void loginClicked() {
		view.hideModal();
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
	}
	
	public boolean isAnonymous() {
		return !authenticationController.isLoggedIn();
	}
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void cancelClicked() {
		finished();
	}
	
}
