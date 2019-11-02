package org.sagebionetworks.web.client.presenter;

import static com.google.common.util.concurrent.Futures.getDone;
import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.web.client.DisplayUtils.getDisplayName;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.sagebionetworks.repo.model.InviteeVerificationSignedToken;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseFutureClient;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.EmailInvitation;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.EmailInvitationView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EmailInvitationPresenter extends AbstractActivity implements EmailInvitationView.Presenter, Presenter<EmailInvitation> {
	private String encodedMISignedToken;
	private EmailInvitationView view;
	private SynapseJavascriptClient jsClient;
	private SynapseFutureClient futureClient;
	private SynapseAlert synapseAlert;
	private AuthenticationController authController;
	private PlaceChanger placeChanger;
	private String email;
	private MembershipInvitation membershipInvitation;

	@Inject
	public EmailInvitationPresenter(EmailInvitationView view, SynapseJavascriptClient jsClient, SynapseFutureClient futureClient, SynapseAlert synapseAlert, AuthenticationController authController, GlobalApplicationState globalApplicationState) {
		this.view = view;
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

		futureClient.hexDecodeAndDeserialize(encodedMISignedToken).transformAsync(token -> jsClient.getMembershipInvitation((MembershipInvtnSignedToken) token), directExecutor()).addCallback(new FutureCallback<MembershipInvitation>() {
			@Override
			public void onSuccess(MembershipInvitation mis) {
				membershipInvitation = mis;
				// check to see if this invitation is associated to the current user
				if (authController.isLoggedIn()) {
					if (mis.getInviteeId() != null && authController.getCurrentUserPrincipalId().equals(mis.getInviteeId())) {
						// user id already bound to invitation. redirect to profile page
						gotoProfilePageTeamArea();
						return;
					} else {
						verifyEmailAssociatedToAuthenticatedUser();
					}
				}
				initializeView(mis);
			}

			@Override
			public void onFailure(Throwable throwable) {
				view.hideLoading();
				synapseAlert.handleException(throwable);
			}
		}, directExecutor());

	}

	private void verifyEmailAssociatedToAuthenticatedUser() {
		// verify the currently logged in user is associated to the given membership invitation
		jsClient.getInviteeVerificationSignedToken(membershipInvitation.getId()).addCallback(new FutureCallback<InviteeVerificationSignedToken>() {
			@Override
			public void onSuccess(@NullableDecl InviteeVerificationSignedToken token) {
				bindInvitationToAuthenticatedUser(token);
			}

			@Override
			public void onFailure(Throwable t) {
				view.hideLoading();
				if (t instanceof ForbiddenException) {
					// SWC-4721: fix message in the case where membership invitation email is not associated to the
					// currently logged in user
					view.showErrorMessage("This invitation was sent to an email address not associated to the current user. \"" + membershipInvitation.getInviteeEmail() + "\" Please add this email to your Synapse account under \"Settings\", or log in with the correct Synapse account before accepting the invitation.");
					// SWC-4741: invitation not associated to the current user, send user to the Settings page to add
					// the new email address
					placeChanger.goTo(new Profile(authController.getCurrentUserPrincipalId(), ProfileArea.SETTINGS));
				} else {
					synapseAlert.handleException(t);
				}
			}
		}, directExecutor());
	}

	private void bindInvitationToAuthenticatedUser(InviteeVerificationSignedToken token) {
		jsClient.updateInviteeId(token).addCallback(new FutureCallback<Void>() {
			@Override
			public void onSuccess(Void t) {
				// SWC-4759: attempt to Join team (user clicked on a Join button in the email to get here).
				addTeamMember();
			}

			@Override
			public void onFailure(Throwable t) {
				view.hideLoading();
				synapseAlert.handleException(t);
			}
		}, directExecutor());
	}

	private void addTeamMember() {
		jsClient.addTeamMember(authController.getCurrentUserPrincipalId(), membershipInvitation.getTeamId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Successfully joined the team.");
				placeChanger.goTo(new org.sagebionetworks.web.client.place.Team(membershipInvitation.getTeamId()));
			}

			@Override
			public void onFailure(Throwable caught) {
				// bound to invitation, but could not add directly
				synapseAlert.consoleError("Unable to add user directly to team, sending to team area of profile: " + caught.getMessage());
				gotoProfilePageTeamArea();
			}
		});
	}


	private void gotoProfilePageTeamArea() {
		// SWC-4668: take the user to their profile page with the join request for them to accept (using the
		// Join button, which handles any ARs)
		placeChanger.goTo(new Profile(authController.getCurrentUserPrincipalId(), ProfileArea.TEAMS));
	}

	private void initializeView(final MembershipInvitation mis) {
		view.showNotLoggedInUI();
		this.email = mis.getInviteeEmail();
		view.hideLoading();

		ListenableFuture<Team> teamFuture;
		ListenableFuture<UserProfile> userProfileFuture;

		teamFuture = jsClient.getTeam(mis.getTeamId());
		userProfileFuture = jsClient.getUserProfile(mis.getCreatedBy());
		FluentFuture.from(whenAllComplete(teamFuture, userProfileFuture).call(() -> {
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
		}, directExecutor())).catching(Throwable.class, e -> {
			synapseAlert.handleException(e);
			return null;
		}, directExecutor());
	}

	@Override
	public void onLoginClick() {
		placeChanger.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
	}

	@Override
	public void onRegisterClick() {
		placeChanger.goTo(new RegisterAccount(email));
	}
}
