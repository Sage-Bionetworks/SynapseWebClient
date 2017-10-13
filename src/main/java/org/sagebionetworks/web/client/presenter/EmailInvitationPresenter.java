package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.plugins.deferred.PromiseRPC;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.sagebionetworks.repo.model.*;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.EmailInvitation;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.EmailInvitationView;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.NotificationTokenType;

import javax.inject.Inject;

import static org.sagebionetworks.web.client.DisplayUtils.getDisplayName;

public class EmailInvitationPresenter extends AbstractActivity implements EmailInvitationView.Presenter, Presenter<EmailInvitation> {
	private EmailInvitationView view;
	private RegisterWidget registerWidget;
	private SynapseClientAsync synapseClient;
	private SynapseAlert synapseAlert;
	private AuthenticationController authController;
	private PlaceChanger placeChanger;

	@Inject
	public EmailInvitationPresenter(EmailInvitationView view,
									RegisterWidget registerWidget,
									SynapseClientAsync synapseClient,
									SynapseAlert synapseAlert,
									AuthenticationController authController,
									GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.registerWidget = registerWidget;
		this.synapseClient = synapseClient;
		this.synapseAlert = synapseAlert;
		this.authController = authController;
		this.placeChanger = globalApplicationState.getPlaceChanger();
		view.setPresenter(this);
		registerWidget.enableEmailAddressField(false);
		view.setRegisterWidget(registerWidget.asWidget());
		view.setSynapseAlertContainer(synapseAlert.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
		acceptsOneWidget.setWidget(view);
	}

	@Override
	public void setPlace(EmailInvitation place) {
	    final String encodedMembershipInvtnSignedToken = place.toToken();
		PromiseRPC<SignedTokenInterface> promise = new PromiseRPC<>();
		synapseClient.hexDecodeAndDeserialize(NotificationTokenType.EmailInvitation.name(), encodedMembershipInvtnSignedToken, promise);
		promise.then(new Function() {
			@Override
			public PromiseRPC<MembershipInvtnSubmission> f(Object... args) {
				MembershipInvtnSignedToken token = (MembershipInvtnSignedToken) args[0];
				PromiseRPC<MembershipInvtnSubmission> promise = new PromiseRPC<>();
				synapseClient.getMembershipInvitation(token, promise);
				return promise;
			}
		}).done(new Function() {
			@Override
			public void f() {
				MembershipInvtnSubmission mis = (MembershipInvtnSubmission) arguments(0);
				if (authController.isLoggedIn()) {
					bindInvitationToAuthenticatedUser(mis.getId());
				} else {
					EmailInvitationPresenter.this.registerWidget.setEncodedMembershipInvtnSignedToken(encodedMembershipInvtnSignedToken);
					EmailInvitationPresenter.this.registerWidget.setEmail(mis.getInviteeEmail());
					setupInvitationMessage(mis);
				}
			}
		}).fail(new Function() {
			@Override
			public void f() {
				Throwable throwable = arguments(0);
				synapseAlert.handleException(throwable);
			}
		});
	}

	private void bindInvitationToAuthenticatedUser(final String misId) {
		PromiseRPC<InviteeVerificationSignedToken> promise = new PromiseRPC<>();
		synapseClient.getInviteeVerificationSignedToken(misId, promise);
		promise.then(new Function() {
            @Override
            public PromiseRPC<Void> f(Object... args) {
                InviteeVerificationSignedToken token = (InviteeVerificationSignedToken) args[0];
                PromiseRPC<Void> promise = new PromiseRPC<>();
                synapseClient.updateInviteeId(misId, token, promise);
                return promise;
            }
        }).done(new Function() {
            @Override
            public void f() {
                placeChanger.goTo(new Profile(authController.getCurrentUserPrincipalId(), Synapse.ProfileArea.TEAMS));
            }
        }).fail(new Function() {
            @Override
            public void f() {
                Throwable throwable = arguments(0);
                synapseAlert.handleException(throwable);
            }
        });
	}

	private void setupInvitationMessage(final MembershipInvtnSubmission mis) {
		PromiseRPC<Team> teamPromise = new PromiseRPC<>();
		PromiseRPC<UserProfile> userPromise = new PromiseRPC<>();
		synapseClient.getTeam(mis.getTeamId(), teamPromise);
		synapseClient.getUserProfile(mis.getCreatedBy(), userPromise);
		GQuery.when(teamPromise, userPromise)
		.done(new Function() {
            public void f() {
                Team team = (Team) arguments(0, 0);
				UserProfile userProfile = (UserProfile) arguments(1, 0);
				String title = "You have been invited to join ";
				String teamName = team.getName();
				if (teamName != null) {
					title += " the team " + teamName;
				} else {
					title += " a team";
				}
				String displayName = getDisplayName(userProfile);
				if (displayName != null) {
					title += " by " + displayName;
				}
                EmailInvitationPresenter.this.view.setInvitationTitle(title);
				String message = mis.getMessage();
				if (message != null) {
					EmailInvitationPresenter.this.view.setInvitationMessage(mis.getMessage());
				}
            }
        }).fail(new Function() {
            @Override
            public void f() {
                Throwable throwable = arguments(0);
                synapseAlert.handleException(throwable);
            }
        });
	}

	@Override
	public void onLoginClick() {
		placeChanger.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
	}
}
