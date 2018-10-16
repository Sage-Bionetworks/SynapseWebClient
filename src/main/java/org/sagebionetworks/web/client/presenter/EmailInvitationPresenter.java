package org.sagebionetworks.web.client.presenter;

import static com.google.common.util.concurrent.Futures.getDone;
import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.web.client.DisplayUtils.getDisplayName;

import javax.inject.Inject;

import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseFutureClient;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.EmailInvitation;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.EmailInvitationView;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.NotificationTokenType;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EmailInvitationPresenter extends AbstractActivity implements EmailInvitationView.Presenter, Presenter<EmailInvitation> {
	private String encodedMISignedToken;
	private EmailInvitationView view;
	private RegisterWidget registerWidget;
	private SynapseJavascriptClient jsClient;
	private SynapseFutureClient futureClient;
	private SynapseAlert synapseAlert;
	private AuthenticationController authController;
	private PlaceChanger placeChanger;
	private String teamId;
	@Inject
	public EmailInvitationPresenter(
			EmailInvitationView view,
			RegisterWidget registerWidget,
			SynapseJavascriptClient jsClient,
			SynapseFutureClient futureClient,
			SynapseAlert synapseAlert,
			AuthenticationController authController,
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.registerWidget = registerWidget;
		this.registerWidget.enableEmailAddressField(false);
		this.view.setRegisterWidget(this.registerWidget.asWidget());
		this.jsClient = jsClient;
		this.futureClient = futureClient;
		this.synapseAlert = synapseAlert;
		this.view.setSynapseAlertContainer(this.synapseAlert.asWidget());
		this.authController = authController;
		this.placeChanger = globalApplicationState.getPlaceChanger();
	}

	@Override
	public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
		acceptsOneWidget.setWidget(view);
	}

	@Override
	public void setPlace(EmailInvitation place) {
		view.showLoading();
		view.clear();
		view.setPresenter(this);
		encodedMISignedToken = place.toToken();

		futureClient.hexDecodeAndDeserialize(NotificationTokenType.EmailInvitation.name(), encodedMISignedToken)
			.transformAsync(
					token -> jsClient.getMembershipInvitation((MembershipInvtnSignedToken) token),
					directExecutor()
			).addCallback(
					new FutureCallback<MembershipInvitation>() {
						@Override
						public void onSuccess(MembershipInvitation mis) {
							teamId = mis.getTeamId();
							if (!authController.isLoggedIn()) {
								initializeView(mis);
							} else {
								bindInvitationToAuthenticatedUser(mis.getId());
							}
						}

						@Override
						public void onFailure(Throwable throwable) {
							view.hideLoading();
							synapseAlert.handleException(throwable);
						}
					},
					directExecutor()
			);

	}

	private void bindInvitationToAuthenticatedUser(final String misId) {
		jsClient.getInviteeVerificationSignedToken(misId)
			.transformAsync(
					token -> jsClient.updateInviteeId(token),
					directExecutor()
			).addCallback(
					new FutureCallback<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							placeChanger.goTo(new org.sagebionetworks.web.client.place.Team(teamId));
						}

						@Override
						public void onFailure(Throwable throwable) {
							view.hideLoading();
							synapseAlert.handleException(throwable);
						}
					},
					directExecutor()
			);
	}

	private void initializeView(final MembershipInvitation mis) {
		view.showNotLoggedInUI();
		registerWidget.setEncodedMembershipInvtnSignedToken(encodedMISignedToken);
		registerWidget.setEmail(mis.getInviteeEmail());
		view.hideLoading();

		ListenableFuture<Team> teamFuture;
		ListenableFuture<UserProfile> userProfileFuture;

		teamFuture = jsClient.getTeam(mis.getTeamId());
		userProfileFuture = jsClient.getUserProfile(mis.getCreatedBy());
		FluentFuture.from(whenAllComplete(teamFuture, userProfileFuture)
				.call(() -> {
							// Retrieve the resolved values from the futures
							Team team = getDone(teamFuture);
							UserProfile userProfile = getDone(userProfileFuture);
							// Build the message
							String title = "You have been invited to join ";
							if (team.getName() != null) {
								title += " the team " + team.getName();
							} else {
								title += " a team";
							}
							String displayName = getDisplayName(userProfile);
							if (displayName != null) {
								title += " by " + displayName;
							}
							view.setInvitationTitle(title);
							String message = mis.getMessage();
							if (message != null) {
								view.setInvitationMessage(mis.getMessage());
							}
							return null;
						},
						directExecutor())
				).catching(
						Throwable.class,
						e -> {
							synapseAlert.handleException(e);
							return null;
						},
						directExecutor()
				);
	}

	@Override
	public void onLoginClick() {
		placeChanger.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
	}
}
