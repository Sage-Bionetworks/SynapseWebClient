package org.sagebionetworks.web.client.presenter;

import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.ResponseMessage;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.message.NotificationSettingsSignedToken;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.SignedToken;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.SignedTokenView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SignedTokenPresenter extends AbstractActivity implements SignedTokenView.Presenter, Presenter<SignedToken> {
	private SignedToken place;
	private SignedTokenView view;
	private SynapseClientAsync synapseClient;
	private GWTWrapper gwt;
	private SynapseAlert synapseAlert;
	private GlobalApplicationState globalApplicationState;
	SignedTokenInterface signedToken;
	UserBadge unsubscribingUserBadge;
	AuthenticationController authController;
	boolean isFirstTry;
	@Inject
	public SignedTokenPresenter(SignedTokenView view, 
			SynapseClientAsync synapseClient, 
			GWTWrapper gwt, 
			SynapseAlert synapseAlert,
			GlobalApplicationState globalApplicationState, 
			UserBadge unsubscribingUserBadge,
			AuthenticationController authController){
		this.view = view;
		this.synapseClient = synapseClient;
		this.synapseAlert = synapseAlert;
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
		this.place = place;
		this.view.setPresenter(this);
		
		configure(place.getTokenType(), place.getSignedEncodedToken());
	}
	
	public void configure(String tokenType, String signedEncodedToken) {
		signedToken = null;
		synapseAlert.clear();
		view.clear();
		view.setLoadingVisible(true);
		//hex decode the token
		synapseClient.hexDecodeAndDeserialize(tokenType, signedEncodedToken, new AsyncCallback<JSONEntity>() {
			@Override
			public void onSuccess(JSONEntity result) {
				view.setLoadingVisible(false);
				if (result instanceof SignedTokenInterface) {
					signedToken = (SignedTokenInterface) result;
					if (result instanceof NotificationSettingsSignedToken) {
						handleSettingsToken();
					} else if (result instanceof JoinTeamSignedToken) {
						isFirstTry = true;
						handleJoinTeamToken();
					} else {
						handleSignedToken();
					}
				} else {
					synapseAlert.showError("token is not a signed token");
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setLoadingVisible(false);
				synapseAlert.handleException(caught);
			}
		});
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
				view.showSuccess(result.getMessage());
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
		globalApplicationState.gotoLastPlace();
	}
}
