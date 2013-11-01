package org.sagebionetworks.web.client.widget.team;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinTeamWidget implements JoinTeamWidgetView.Presenter, WidgetRendererPresenter {
	private JoinTeamWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private String teamId;
	private boolean showUserProfileForm;
	private AuthenticationController authenticationController;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	private Callback teamUpdatedCallback;
	private String message;
	private boolean isAcceptingInvite, canPublicJoin;
	private Callback widgetRefreshRequired;
	
	@Inject
	public JoinTeamWidget(JoinTeamWidgetView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState, 
			AuthenticationController authenticationController, 
			NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter
			) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
	}

	public void configure(String teamId, boolean canPublicJoin, boolean showUserProfileForm, TeamMembershipStatus teamMembershipStatus, Callback teamUpdatedCallback) {
		//set team id
		this.teamId = teamId;
		this.canPublicJoin = canPublicJoin;
		this.showUserProfileForm = showUserProfileForm;
		this.teamUpdatedCallback = teamUpdatedCallback;
		view.configure(authenticationController.isLoggedIn(), canPublicJoin, teamMembershipStatus);
	};
//	
//	@Override
//	public void deleteAllJoinRequests() {
//		synapseClient.deleteOpenMembershipRequests(authenticationController.getCurrentUserPrincipalId(), teamId, new AsyncCallback<Void>() {
//			@Override
//			public void onSuccess(Void result) {
//				view.showInfo("Cancelled Request", "The request to join the team has been removed.");
//				teamUpdatedCallback.invoke();
//			}
//			@Override
//			public void onFailure(Throwable caught) {
//				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
//					view.showErrorMessage(caught.getMessage());
//				} 
//			}
//		});
//	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> descriptor, Callback widgetRefreshRequired) {
		this.widgetRefreshRequired = widgetRefreshRequired;
		this.teamId = null;
		if (descriptor.containsKey(WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY)) 
			this.teamId = descriptor.get(WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY);
		
		this.showUserProfileForm = descriptor.containsKey(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY) ? 
				Boolean.parseBoolean(descriptor.get(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY)) : 
				false;
		refresh();
	}
	
	private void refresh() {
		boolean isLoggedIn = authenticationController.isLoggedIn();
		if (isLoggedIn) {
			synapseClient.getTeamBundle(authenticationController.getCurrentUserPrincipalId(), teamId, isLoggedIn, new AsyncCallback<TeamBundle>() {
				@Override
				public void onSuccess(TeamBundle result) {
					try {
						Team team = nodeModelCreator.createJSONEntity(result.getTeamJson(), Team.class);
						TeamMembershipStatus teamMembershipStatus = null;
						if (result.getTeamMembershipStatusJson() != null)
							teamMembershipStatus = nodeModelCreator.createJSONEntity(result.getTeamMembershipStatusJson(), TeamMembershipStatus.class);
						configure(team.getId(), team.getCanPublicJoin(), showUserProfileForm, teamMembershipStatus, null);
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		} else {
			configure(teamId, canPublicJoin, showUserProfileForm, null, null);
		}
	}
	
	@Override
	public void sendJoinRequest(String message, boolean isAcceptingInvite) {
		this.message = message;
		this.isAcceptingInvite = isAcceptingInvite;
		if (showUserProfileForm)
			sendJoinRequestStep1();
		else //skip to step 2
			sendJoinRequestStep2();
	}
	

	/**
	 * Gather additional info about the logged in user
	 */
	public void sendJoinRequestStep1() {
		//pop up profile form.  user does not have to fill in info
		UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
		UserProfile profile = sessionData.getProfile();
		view.showProfileForm(profile, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				continueToStep2();
			}
			@Override
			public void onFailure(Throwable caught) {
				continueToStep2();
			}
			
			public void continueToStep2(){
				sendJoinRequestStep2();	
			}
		});
	}
	

	/**
	 * Check for unmet access restrictions. As long as more exist, it will keep calling itself until all restrictions are approved.
	 * Will not proceed to step3 (joining the team) until all have been approved.
	 * @throws RestServiceException
	 */
	public void sendJoinRequestStep2() {
		synapseClient.getUnmetTeamAccessRequirements(teamId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				//are there unmet access restrictions?
				try{
					PaginatedResults<AccessRequirement> ar = nodeModelCreator.createPaginatedResults(result, AccessRequirement.class);
					if (ar.getTotalNumberOfResults() > 0) {
						//there are unmet access requirements.  user must accept all before joining the challenge
						List<AccessRequirement> unmetRequirements = ar.getResults();
						final AccessRequirement firstUnmetAccessRequirement = unmetRequirements.get(0);
						String text = GovernanceServiceHelper.getAccessRequirementText(firstUnmetAccessRequirement);
						Callback termsOfUseCallback = new Callback() {
							@Override
							public void invoke() {
								//agreed to terms of use.
								setLicenseAccepted(firstUnmetAccessRequirement.getId());
							}
						};
						//pop up the requirement
						view.showAccessRequirement(text, termsOfUseCallback);
					} else {
						sendJoinRequestStep3();
					}
						
				} catch (Throwable e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.JOIN_TEAM_ERROR + caught.getMessage());
			}
		});
	}
	
	public void setLicenseAccepted(Long arId) {	
		final CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
			@Override
			public void invoke(Throwable t) {
				view.showErrorMessage(DisplayConstants.JOIN_TEAM_ERROR + t.getMessage());
			}
		};
		
		Callback onSuccess = new Callback() {
			@Override
			public void invoke() {
				//ToU signed, now try to register for the challenge (will check for other unmet access restrictions before join)
				sendJoinRequestStep2();
			}
		};
		
		GovernanceServiceHelper.signTermsOfUse(
				authenticationController.getCurrentUserPrincipalId(), 
				arId, 
				onSuccess, 
				onFailure, 
				synapseClient, 
				jsonObjectAdapter);
	}
	
	public void sendJoinRequestStep3() {
		synapseClient.requestMembership(authenticationController.getCurrentUserPrincipalId(), teamId, message, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				String message = isAcceptingInvite ? "Invitation Accepted" : "Request Sent";
				view.showInfo(message, "");
				refresh();
				if (teamUpdatedCallback != null)
					teamUpdatedCallback.invoke();
				if (widgetRefreshRequired != null)
					widgetRefreshRequired.invoke();
				
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@Override
	public void gotoLoginPage() {
		goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
	}
	
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	
	public void clear() {
		view.clear();
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
}
