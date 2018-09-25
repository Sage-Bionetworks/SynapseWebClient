package org.sagebionetworks.web.client.widget.team;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.SelfSignAccessRequirementInterface;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinTeamWidget implements JoinTeamWidgetView.Presenter, WidgetRendererPresenter {
	private JoinTeamWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private MarkdownWidget wikiPage;
	private WizardProgressWidget progressWidget;
	private SynapseAlert synAlert;
	private GWTWrapper gwt;
	private CookieProvider cookies;
	private String teamId;
	private boolean isChallengeSignup;
	private AuthenticationController authenticationController;
	private Callback teamUpdatedCallback;
	private String message, isMemberMessage, successMessage, buttonText, requestOpenInfoText;
	private Integer requestExpiresInXDays;
	private boolean isSimpleRequestButton;
	private Callback widgetRefreshRequired;
	private List<AccessRequirement> accessRequirements;
	private int currentPage;
	private int currentAccessRequirement;
	
	public static final String[] EXTRA_INFO_URL_WHITELIST = { 
		"https://www.projectdatasphere.org/projectdatasphere/",
		"https://mpmdev.ondemand.sas.com/projectdatasphere/"
	};
	String accessRequirementsUrl;
	
	@Inject
	public JoinTeamWidget(JoinTeamWidgetView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState, 
			AuthenticationController authenticationController,
			GWTWrapper gwt,
			MarkdownWidget wikiPage, 
			WizardProgressWidget progressWidget,
			SynapseAlert synAlert,
			CookieProvider cookies
			) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.gwt = gwt;
		this.wikiPage = wikiPage;
		this.progressWidget = progressWidget;
		this.synAlert = synAlert;
		this.cookies = cookies;
		view.setProgressWidget(progressWidget);
		view.setSynAlert(synAlert);
	}
	
	/**
	 * Simple join button configuration.  Give a team to join, it will invoke the callback when the user successfully joins the team. 
	 * @param teamId
	 * @param callback
	 */
	public void configure(String teamId, Callback callback) {
		this.teamId = teamId;
		this.isChallengeSignup = false;
		this.isSimpleRequestButton = true;
		this.successMessage = null;
		this.buttonText = null;
		this.requestOpenInfoText = null;
		this.widgetRefreshRequired = callback;
		this.requestExpiresInXDays = null;
		refresh();
	}
	
	public void configure(String teamId, boolean isChallengeSignup, TeamMembershipStatus teamMembershipStatus, 
			Callback teamUpdatedCallback, String isMemberMessage, String successMessage, String buttonText, String requestOpenInfoText, boolean isSimpleRequestButton) {
		//set team id
		this.teamId = teamId;
		this.isChallengeSignup = isChallengeSignup;
		this.isSimpleRequestButton = isSimpleRequestButton;
		this.teamUpdatedCallback = teamUpdatedCallback;
		this.isMemberMessage = isMemberMessage;
		this.successMessage = successMessage;
		this.buttonText = buttonText;
		view.clear();
		synAlert.clear();
		accessRequirementsUrl = "#!AccessRequirements:"+AccessRequirementsPlace.ID_PARAM + "=" + teamId + "&" + AccessRequirementsPlace.TYPE_PARAM + "=" + RestrictableObjectType.TEAM.toString();
		if (buttonText != null && !buttonText.isEmpty()) {
			view.setJoinButtonsText(buttonText);
		}
		if (requestOpenInfoText != null && !requestOpenInfoText.isEmpty()) {
			view.setRequestOpenText(requestOpenInfoText);
		}
		if (isMemberMessage != null && !isMemberMessage.isEmpty()) {
			view.setIsMemberMessage(SafeHtmlUtils.htmlEscape(isMemberMessage));
		}
		boolean isLoggedIn = authenticationController.isLoggedIn();
		if (isLoggedIn) {
			view.setUserPanelVisible(true);
			//(note:  in all cases, clicking UI will check for unmet ToU)
			if (teamMembershipStatus.getIsMember()) {
				if (isMemberMessage != null && isMemberMessage.length() > 0) {
					view.setIsMemberMessageVisible(true);
				}
			} else if (teamMembershipStatus.getCanJoin()) { // not in team but can join with a single request
				// show join button; clicking Join joins the team
				view.setSimpleRequestButtonVisible(true);
			} else if (teamMembershipStatus.getHasOpenRequest()) {
				// display a message saying "your membership request is pending review by team administration"
				view.setRequestMessageVisible(true);
			} else if (teamMembershipStatus.getMembershipApprovalRequired() && !teamMembershipStatus.getHasOpenInvitation()) {
				// show request UI
				if (isSimpleRequestButton) {
					view.setSimpleRequestButtonVisible(true);
				} else {
					view.setRequestButtonVisible(true);
				}
			} else if (teamMembershipStatus.getHasUnmetAccessRequirement()) {
			    // show Join; clicking shows ToU
				view.setAcceptInviteButtonVisible(true);
			} else {
			    // illegal state
				synAlert.showError("Unable to determine state");
			}
		} else {
			view.setUserPanelVisible(false);
			view.setAnonUserButtonVisible(true);
		}
	};

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> descriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.widgetRefreshRequired = widgetRefreshRequired;
		this.teamId = null;
		if (descriptor.containsKey(WidgetConstants.TEAM_ID_KEY)) 
			this.teamId = descriptor.get(WidgetConstants.TEAM_ID_KEY);
		
		//is the team associated with joining a challenge?
		if (descriptor.containsKey(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY)) {
			this.isChallengeSignup = Boolean.parseBoolean(descriptor.get(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY));
		} else {
			//check for old param
			this.isChallengeSignup = descriptor.containsKey(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY) ? 
					Boolean.parseBoolean(descriptor.get(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY)) : 
					false;
		}
		this.isSimpleRequestButton = false;
		if (descriptor.containsKey(WidgetConstants.JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON)) {
			this.isSimpleRequestButton = Boolean.parseBoolean(descriptor.get(WidgetConstants.JOIN_TEAM_IS_SIMPLE_REQUEST_BUTTON));
		}
		this.requestExpiresInXDays = null;
		if (descriptor.containsKey(WidgetConstants.JOIN_WIDGET_REQUEST_EXPIRES_IN_X_DAYS_KEY)) {
			this.requestExpiresInXDays = Integer.parseInt(descriptor.get(WidgetConstants.JOIN_WIDGET_REQUEST_EXPIRES_IN_X_DAYS_KEY));
		}
		this.isMemberMessage = descriptor.get(WidgetConstants.IS_MEMBER_MESSAGE);
		
		this.successMessage = descriptor.get(WidgetConstants.SUCCESS_MESSAGE);
		this.buttonText = descriptor.get(WidgetConstants.JOIN_TEAM_BUTTON_TEXT);
		this.requestOpenInfoText = descriptor.get(WidgetConstants.JOIN_TEAM_OPEN_REQUEST_TEXT);
		refresh();
	}
	
	private void refresh() {
		boolean isLoggedIn = authenticationController.isLoggedIn();
		if (isLoggedIn) {
			synAlert.clear();
			synapseClient.getTeamBundle(authenticationController.getCurrentUserPrincipalId(), teamId, isLoggedIn, new AsyncCallback<TeamBundle>() {
				@Override
				public void onSuccess(TeamBundle result) {
					Team team = result.getTeam();
					TeamMembershipStatus teamMembershipStatus = null;
					if (result.getTeamMembershipStatus() != null)
						teamMembershipStatus = result.getTeamMembershipStatus();
					configure(team.getId(), isChallengeSignup, teamMembershipStatus, null, isMemberMessage, successMessage, buttonText, requestOpenInfoText, isSimpleRequestButton);
				}
				@Override
				public void onFailure(Throwable caught) {
					view.hideJoinWizard();
					synAlert.handleException(caught);
				}
			});
		} else {
			configure(teamId, isChallengeSignup, null, null, isMemberMessage, successMessage, buttonText, requestOpenInfoText, isSimpleRequestButton);
		}
	}
	
	@Override
	public void sendJoinRequest(String message) {
		this.message = message;
		sendJoinRequestStep0();
	}
	
	public void sendJoinRequestStep0() {
		view.setAccessRequirementsLinkVisible(false);
		view.setButtonsEnabled(false);
		currentPage = 0;
		currentAccessRequirement = 0;
		//initialize the access requirements
		accessRequirements = new ArrayList<AccessRequirement>();
		synapseClient.getTeamAccessRequirements(teamId, new AsyncCallback<List<AccessRequirement>>() {
			@Override
			public void onSuccess(List<AccessRequirement> results) {
				//are there access restrictions?
				accessRequirements = results;
				view.setButtonsEnabled(true);
				//access requirements initialized, show the join wizard if challenge signup, or if there are AR to show
				if (isChallengeSignup) {
					startChallengeSignup();
				}
				else { //skip to step 2
					if (accessRequirements.size() > 0) {
						view.showJoinWizard();
					}
					sendJoinRequestStep2();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.setButtonsEnabled(true);
				synAlert.handleException(caught);
			}
		});
	}
	
	public void startChallengeSignup() {
		//first, get the challenge participation info wiki key, then show the join wizard
		CallbackP<WikiPageKey> callback = new CallbackP<WikiPageKey>(){
			@Override
			public void invoke(WikiPageKey key) {
				view.showJoinWizard();
				sendJoinRequestStep1(key);
			}
		};
		getChallengeParticipationInfoWikiKey(callback);
	}
	
	public void getChallengeParticipationInfoWikiKey(final CallbackP<WikiPageKey> callback) {
		synapseClient.getPageNameToWikiKeyMap(new AsyncCallback<HashMap<String,WikiPageKey>>() {
			@Override
			public void onSuccess(HashMap<String,WikiPageKey> result) {
				callback.invoke(result.get(WebConstants.CHALLENGE_PARTICIPATION_INFO));
			};
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				callback.invoke(null);
			}
		});
	}

	public int getTotalPageCount() {
		int challengeSignupPage = isChallengeSignup ? 1 : 0;
		return accessRequirements.size() + challengeSignupPage;
	}
	
	/**
	 * Gather additional info about the logged in user
	 */
	public void sendJoinRequestStep1(WikiPageKey challengeInfoWikiPageKey) {
		progressWidget.configure(currentPage, getTotalPageCount());
		view.setJoinWizardCallback(new Callback() {
			@Override
			public void invoke() {
				currentPage++;
				sendJoinRequestStep2();
			}
		});
		wikiPage.loadMarkdownFromWikiPage(challengeInfoWikiPageKey, false);
		view.setCurrentWizardContent(wikiPage);			
	}
	
	/**
	 * Check for unmet access restrictions. As long as more exist, it will keep calling itself until all restrictions are approved.
	 * Will not proceed to step3 (joining the team) until all have been approved.
	 * @throws RestServiceException
	 */
	public void sendJoinRequestStep2() {
		if (currentAccessRequirement >= accessRequirements.size()) {
			//done showing access requirements (and challenge info)
			view.hideJoinWizard();
			sendJoinRequestStep3();
		} else {
			final AccessRequirement accessRequirement = accessRequirements.get(currentAccessRequirement);
			view.setJoinWizardCallback(new Callback() {
				@Override
				public void invoke() {
					//agreed to terms of use.
					currentAccessRequirement++;
					currentPage++;
					setLicenseAccepted(accessRequirement);
				}
			});
			//pop up the requirement
			progressWidget.configure(currentPage, getTotalPageCount());
			
			if (accessRequirement instanceof SelfSignAccessRequirementInterface || 
					accessRequirement instanceof ACTAccessRequirement ||
					accessRequirement instanceof ManagedACTAccessRequirement) {
				String text = GovernanceServiceHelper.getAccessRequirementText(accessRequirement);
				if (text == null || text.trim().isEmpty()) {
					WikiPageKey wikiKey = new WikiPageKey(accessRequirement.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), null);
					boolean isIgnoreLoadingFailure=true;
					wikiPage.loadMarkdownFromWikiPage(wikiKey, isIgnoreLoadingFailure);
					view.setAccessRequirementHTML("");
					view.setCurrentWizardPanelVisible(true);
					view.setCurrentWizardContent(wikiPage);
				} else {
					view.setAccessRequirementHTML(text);
					view.setCurrentWizardPanelVisible(false);
				}
				boolean isACTAccessRequirement = accessRequirement instanceof ACTAccessRequirement || accessRequirement instanceof ManagedACTAccessRequirement;
				String primaryButtonText = isACTAccessRequirement ? "Continue" : "Accept";
				view.setJoinWizardPrimaryButtonText(primaryButtonText);
				view.setAccessRequirementsLinkVisible(isACTAccessRequirement);
			} else {
				synAlert.showError("Unsupported access restriction type - " + accessRequirement.getClass().getName());
			}
		}
	}
	
	public String getEncodedParamValue(String paramKey, String value, String suffix) {
		String param = paramKey+"=";
		if (DisplayUtils.isDefined(value)) {
			param += gwt.encodeQueryString(value);
		}
		param += suffix;
		return param;
	}
	
	/**
	 * return true if it is on a whitelist that allows Synapse to send additional information in the query params
	 * @param siteUrl
	 * @return
	 */
	public static boolean isRecognizedSite(String siteUrl) {
		if(siteUrl != null) {
			for(String base : EXTRA_INFO_URL_WHITELIST) {
				// starts with one of the valid url bases?				
				if(siteUrl.toLowerCase().startsWith(base)) return true;
			}
		}
		return false;
	}

	
	public void setLicenseAccepted(AccessRequirement ar) {	
		AsyncCallback<AccessApproval> callback = new AsyncCallback<AccessApproval>() {
			@Override
			public void onSuccess(AccessApproval result) {
				//ToU signed, now try to register for the challenge (will check for other access restrictions before join)
				sendJoinRequestStep2();
			}
			@Override
			public void onFailure(Throwable t) {
				synAlert.handleException(t);
			}
		};
		if (ar instanceof ACTAccessRequirement || ar instanceof ManagedACTAccessRequirement) {
			//no need to sign, just continue
			callback.onSuccess(null);
		} else {
			GovernanceServiceHelper.signTermsOfUse(
					authenticationController.getCurrentUserPrincipalId(), 
					ar, 
					synapseClient,
					callback);
		}
	}
	
	public void sendJoinRequestStep3() {
		Date expiresOn = null;
		if (requestExpiresInXDays != null) {
			expiresOn = new Date();
			gwt.addDaysToDate(expiresOn, requestExpiresInXDays);
		}
		synapseClient.requestMembership(authenticationController.getCurrentUserPrincipalId(), teamId, message, gwt.getHostPageBaseURL(), expiresOn, new AsyncCallback<TeamMembershipStatus>() {
			@Override
			public void onSuccess(TeamMembershipStatus teamMembershipStatus) {
				if (teamMembershipStatus.getIsMember()) {
					refresh();
				} else {
					view.showInfo("Request Sent");
				}
				if (teamUpdatedCallback != null)
					teamUpdatedCallback.invoke();
				if (widgetRefreshRequired != null)
					widgetRefreshRequired.invoke();
				
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
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
	
	public boolean isChallengeSignup() {
		return isChallengeSignup;
	}
	
	@Override
	public void onRequestAccess() {
		view.open(accessRequirementsUrl);
	}
}
