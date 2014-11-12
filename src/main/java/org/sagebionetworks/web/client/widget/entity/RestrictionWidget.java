package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.Iterator;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
	private IconsImageBundle iconsImageBundle;
	private HashSet<Long> shownAccessRequirements;
	private Iterator<AccessRequirement> allArsIterator, unmetArsIterator;
	private AccessRequirement currentAR;
	private String jiraFlagLink;
	
	@Inject
	public RestrictionWidget(
			RestrictionWidgetView view,
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalApplicationState,
			JiraURLHelper jiraURLHelper,
			IconsImageBundle iconsImageBundle) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.globalApplicationState = globalApplicationState;
		this.jiraURLHelper = jiraURLHelper;
		this.iconsImageBundle = iconsImageBundle;
		shownAccessRequirements = new HashSet<Long>();
	}
	
	public void configure(EntityBundle bundle, boolean showChangeLink, boolean showIfProject, boolean showFlagLink, com.google.gwt.core.client.Callback<Void, Throwable> entityUpdatedCallback) {
		this.entityUpdatedCallback = entityUpdatedCallback;
		this.showChangeLink = showChangeLink;
		this.showIfProject = showIfProject;
		this.showFlagLink = showFlagLink;
		setEntityBundle(bundle);
	}
	
	public void setEntityBundle(EntityBundle bundle) {
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
		return (bundle.getEntity() instanceof FileEntity) || (bundle.getEntity() instanceof TableEntity) || (bundle.getEntity() instanceof Locationable) || (bundle.getEntity() instanceof Folder) || (showIfProject && bundle.getEntity() instanceof Project);
	}

	public String accessRequirementText() {
		return GovernanceServiceHelper.getAccessRequirementText(getAccessRequirement());
	}
	
	private AccessRequirement getAccessRequirement() {
		return currentAR;
	}
	
	public AccessRequirement selectNextAccessRequirement() {
		AccessRequirement nextAR = null;
		if (unmetArsIterator.hasNext())
			nextAR = unmetArsIterator.next();
		else if (allArsIterator.hasNext())
			nextAR = allArsIterator.next();
		return nextAR;
	}
	
	public void resetAccessRequirementCount() {
		shownAccessRequirements.clear();
		allArsIterator = bundle.getAccessRequirements().iterator();
		unmetArsIterator = bundle.getUnmetAccessRequirements().iterator();
		currentAR = selectNextAccessRequirement();
	}

	public Callback accessRequirementCallback(final AccessRequirement ar) {
		if (APPROVAL_TYPE.USER_AGREEMENT!=GovernanceServiceHelper.accessRequirementApprovalType(getAccessRequirement())) 
			throw new IllegalStateException("not a 'User Agreement' requirement type");
		return new Callback() {
			@Override
			public void invoke() {
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
		configureUI();
		
		return asWidget();
	}
	
	private void configureUI() {
		view.clear();
		final boolean isAnonymous = isAnonymous();
		boolean hasAdministrativeAccess = false;
		
		jiraFlagLink = null;
		if (!isAnonymous) {
			hasAdministrativeAccess = hasAdministrativeAccess();
			jiraFlagLink = getJiraFlagUrl();
		}
		final RESTRICTION_LEVEL restrictionLevel = getRestrictionLevel();
		Callback imposeRestrictionsCallback = getImposeRestrictionsCallback();
		Callback loginCallback = getLoginCallback();
		
		ClickHandler aboutLinkClickHandler = getAboutLinkClickHandler( 
				isAnonymous, 
				 hasAdministrativeAccess,
				 iconsImageBundle,
				 imposeRestrictionsCallback,
				 loginCallback,
				 jiraFlagLink);
		
		switch (restrictionLevel) {
			case OPEN:
				view.showNoRestrictionsUI();
				break;
			case RESTRICTED:
			case CONTROLLED:
				view.showControlledUseUI();
				break;
			default:
				throw new IllegalArgumentException(restrictionLevel.toString());
		}
		
		if(showFlagLink) {
			view.showFlagUI();
		}
		
		//show the info link if there are any restrictions, or if we are supposed to show the flag link (to allow people to flag or  admin to "change" the data access level).
		boolean isChangeLink = restrictionLevel==RESTRICTION_LEVEL.OPEN && hasAdministrativeAccess;
		boolean isRestricted = restrictionLevel!=RESTRICTION_LEVEL.OPEN;
		if ((isChangeLink && showChangeLink) || isRestricted) {
			if (isChangeLink)
				view.showChangeLink(aboutLinkClickHandler);
			else
				view.showShowLink(aboutLinkClickHandler);
		}
		
		if (showFlagLink) {
			if (isAnonymous)
				view.showAnonymousFlagUI();
			else
				view.showFlagUI();
		}
	}
	
	@Override
	public void flagData() {
		view.open(jiraFlagLink);
	}
	
	private ClickHandler getAboutLinkClickHandler(
			final boolean isAnonymous,
			final boolean hasAdministrativeAccess,
			final IconsImageBundle iconsImageBundle,
			final Callback imposeRestrictionsCallback, 
			final Callback loginCallback,
			final String jiraFlagLink  
			 ) {
		 
		return new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					//will run through all access requirements, unmet first (see selectNextAccessRequirement)
					resetAccessRequirementCount();
					showNextAccessRequirement(isAnonymous, hasAdministrativeAccess, iconsImageBundle, imposeRestrictionsCallback, loginCallback, jiraFlagLink);
				}
		};
	}
	
	public void showNextAccessRequirement(
			final boolean isAnonymous,
			final boolean hasAdministrativeAccess,
			final IconsImageBundle iconsImageBundle,
			final Callback imposeRestrictionsCallback, 
			final Callback loginCallback,
			final String jiraFlagLink  
			) {
		
		//iterate over access requirements until we reach one that we have not yet shown (or there are none left to show).
		while(currentAR != null && shownAccessRequirements.contains(currentAR.getId())) {
			currentAR = selectNextAccessRequirement();	
		}
		
		//if there is another access requirement to show, then show it.
		if (currentAR != null) {
			final RESTRICTION_LEVEL restrictionLevel = GovernanceServiceHelper.getRestrictionLevel(currentAR);
			final APPROVAL_TYPE approvalType = getApprovalType();
			shownAccessRequirements.add(currentAR.getId());
			boolean hasFulfilledAccessRequirements = false;
			String accessRequirementText = null;
			Callback touAcceptanceCallback = null;
			Callback requestACTCallback = null;
			if (approvalType!=APPROVAL_TYPE.NONE) {
				accessRequirementText = accessRequirementText();
				if (approvalType==APPROVAL_TYPE.USER_AGREEMENT) {
					touAcceptanceCallback = accessRequirementCallback(currentAR);
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
			
			Callback showNextRestrictionCallback = new Callback() {
				@Override
				public void invoke() {
					showNextAccessRequirement(isAnonymous, hasAdministrativeAccess, iconsImageBundle, imposeRestrictionsCallback, loginCallback, jiraFlagLink);
				}
			};
			
			view.showAccessRequirement(
					restrictionLevel,
					approvalType,
					isAnonymous,
					hasAdministrativeAccess,
					hasFulfilledAccessRequirements,
					iconsImageBundle,
					accessRequirementText,
					imposeRestrictionsCallback,
					touAcceptanceCallback,
					requestACTCallback,
					loginCallback,
					jiraFlagLink,
					showNextRestrictionCallback);
		} else if (hasAdministrativeAccess && bundle.getAccessRequirements().isEmpty()) {
			//there are no access restrictions, and this person has administrative access.  verify data sensitivity, and if try then lockdown
			view.showVerifyDataSensitiveDialog(imposeRestrictionsCallback);
		}
	}
	
	public Callback getImposeRestrictionsCallback() {
		return new Callback() {
			@Override
			public void invoke() {
				view.showLoading();
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
	
	@Override
	public void anonymousFlagModalOkClicked() {
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
	}
	
	public Callback getLoginCallback() {
		return new Callback() {
			public void invoke() {		
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		};
	}

}
