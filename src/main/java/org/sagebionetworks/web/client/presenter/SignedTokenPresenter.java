package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.ResponseMessage;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.message.NotificationSettingsSignedToken;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.place.EmailInvitation;
import org.sagebionetworks.web.client.place.SignedToken;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.SignedTokenView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SignedTokenPresenter extends AbstractActivity implements SignedTokenView.Presenter, Presenter<SignedToken> {
	private SignedTokenView view;
	private SynapseClientAsync synapseClient;
	private GWTWrapper gwt;
	private SynapseAlert synapseAlert;
	private GlobalApplicationState globalApplicationState;
	SignedTokenInterface signedToken;
	UserBadge unsubscribingUserBadge;
	AuthenticationController authController;
	PopupUtilsView popupUtils;
	boolean isFirstTry;
	@Inject
	public SignedTokenPresenter(SignedTokenView view,
								SynapseClientAsync synapseClient,
								GWTWrapper gwt,
								SynapseAlert synapseAlert,
								GlobalApplicationState globalApplicationState,
								UserBadge unsubscribingUserBadge,
								AuthenticationController authController,
								PopupUtilsView popupUtils){
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synapseAlert = synapseAlert;
		this.popupUtils = popupUtils;
		this.gwt = gwt;
		this.globalApplicationState = globalApplicationState;
		this.unsubscribingUserBadge = unsubscribingUserBadge;
		this.authController = authController;
		view.setPresenter(this);
		view.setSynapseAlert(synapseAlert.asWidget());
		view.setUnsubscribingUserBadge(unsubscribingUserBadge.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(SignedToken place) {
		this.view.setPresenter(this);

		configure(place.getTokenType(), place.getSignedEncodedToken());
	}

	public void configure(String tokenType, final String signedEncodedToken) {
		signedToken = null;
		synapseAlert.clear();
		view.clear();
		view.setLoadingVisible(true);
		//hex decode the token
		synapseClient.hexDecodeAndDeserialize(tokenType, signedEncodedToken, new AsyncCallback<SignedTokenInterface>() {
			@Override
			public void onSuccess(SignedTokenInterface result) {
				view.setLoadingVisible(false);
				signedToken = result;
				if (result instanceof NotificationSettingsSignedToken) {
					handleSettingsToken();
				} else if (result instanceof JoinTeamSignedToken) {
					isFirstTry = true;
					handleJoinTeamToken();
				} else if (result instanceof MembershipInvtnSignedToken) {
					handleEmailInvitationToken(signedEncodedToken);
				} else {
					handleSignedToken();
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setLoadingVisible(false);
				synapseAlert.handleException(caught);
			}
		});
	}

	public void handleEmailInvitationToken(final String signedEncodedToken) {
		if (authController.isLoggedIn()) {
			authController.logoutUser();
		}
		globalApplicationState.getPlaceChanger().goTo(new EmailInvitation(signedEncodedToken));
	}

	public void handleSettingsToken() {
		NotificationSettingsSignedToken token = (NotificationSettingsSignedToken) signedToken;
		unsubscribingUserBadge.configure(token.getUserId());
		view.showConfirmUnsubscribe();
	}

	public void handleJoinTeamToken() {
		final JoinTeamSignedToken token = (JoinTeamSignedToken) signedToken;
		String teamId = token.getTeamId();
		view.setLoadingVisible(true);
		// look for access requirements
		synapseClient.getTeamAccessRequirements(teamId, new AsyncCallback<List<AccessRequirement>>() {
			@Override
			public void onSuccess(List<AccessRequirement> accessRequirements) {
				if (accessRequirements.size() > 0) {
					// does not support single click join, go to the team page.
					view.setLoadingVisible(false);
					globalApplicationState.getPlaceChanger().goTo(new Team(token.getTeamId()));
				} else {
					handleSignedToken();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof UnauthorizedException && isFirstTry) {
					// invalid session token.  get rid of it and try again.
					isFirstTry = false;
					authController.logoutUser();
					handleJoinTeamToken();
				} else {
					view.setLoadingVisible(false);
					synapseAlert.handleException(caught);
				}
			}
		});
	}

	public void handleSignedToken() {
		view.clear();
		view.setLoadingVisible(true);
		synapseClient.handleSignedToken(signedToken, gwt.getHostPageBaseURL(), new AsyncCallback<ResponseMessage>() {
			@Override
			public void onSuccess(ResponseMessage result) {
				view.setLoadingVisible(false);
				if (signedToken instanceof JoinTeamSignedToken) {
					// show success message, but send user to the associated Team page.
					popupUtils.showInfo(result.getMessage());
					globalApplicationState.getPlaceChanger().goTo(new Team(((JoinTeamSignedToken)signedToken).getTeamId()));
				} else {
					view.showSuccess(result.getMessage());	
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setLoadingVisible(false);
				synapseAlert.handleException(caught);
			}
		});
	}

	@Override
	public void unsubscribeConfirmed() {
		handleSignedToken();
	}

	@Override
	public String mayStop() {
		view.clear();
		return null;
	}

	@Override
	public void okClicked() {
		globalApplicationState.getPlaceChanger().goTo(AppActivityMapper.getDefaultPlace());
	}
}
