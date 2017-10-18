package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.DisplayUtils.getDisplayName;

import javax.inject.Inject;

import org.sagebionetworks.repo.model.InviteeVerificationSignedToken;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
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

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EmailInvitationPresenter extends AbstractActivity implements EmailInvitationView.Presenter, Presenter<EmailInvitation> {
	private String encodedMembershipInvtnSignedToken;
	private EmailInvitationView view;
	private RegisterWidget registerWidget;
	private SynapseClientAsync synapseClient;
	private SynapseJavascriptClient jsClient;
	private SynapseAlert synapseAlert;
	private AuthenticationController authController;
	private PlaceChanger placeChanger;

	@Inject
	public EmailInvitationPresenter(EmailInvitationView view,
									RegisterWidget registerWidget,
									SynapseClientAsync synapseClient,
									SynapseJavascriptClient jsClient,
									SynapseAlert synapseAlert,
									AuthenticationController authController,
									GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.registerWidget = registerWidget;
		this.synapseClient = synapseClient;
		this.jsClient = jsClient;
		this.synapseAlert = synapseAlert;
		this.authController = authController;
		this.placeChanger = globalApplicationState.getPlaceChanger();
	}

	@Override
	public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
		acceptsOneWidget.setWidget(view);
	}

	@Override
	public void setPlace(EmailInvitation place) {
		view.setPresenter(this);
		view.clear();
		encodedMembershipInvtnSignedToken = place.toToken();
		synapseClient.hexDecodeAndDeserialize(NotificationTokenType.EmailInvitation.name(), encodedMembershipInvtnSignedToken, new AsyncCallback<SignedTokenInterface>() {
			@Override
			public void onFailure(Throwable throwable) {
				synapseAlert.handleException(throwable);
			}

			@Override
			public void onSuccess(SignedTokenInterface token) {
				jsClient.getMembershipInvitation((MembershipInvtnSignedToken) token, new AsyncCallback<MembershipInvtnSubmission>() {
					@Override
					public void onFailure(Throwable throwable) {
						synapseAlert.handleException(throwable);
					}

					@Override
					public void onSuccess(MembershipInvtnSubmission mis) {
						if (authController.isLoggedIn()) {
							bindInvitationToAuthenticatedUser(mis.getId());
						} else {
							initializeView(mis);
						}
					}
				});
			}
		});
	}

	private void bindInvitationToAuthenticatedUser(final String misId) {
		synapseClient.getInviteeVerificationSignedToken(misId, new AsyncCallback<InviteeVerificationSignedToken>() {
			@Override
			public void onFailure(Throwable throwable) {
				synapseAlert.handleException(throwable);
			}

			@Override
			public void onSuccess(InviteeVerificationSignedToken token) {
				synapseClient.updateInviteeId(misId, token, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable throwable) {
						synapseAlert.handleException(throwable);
					}

					@Override
					public void onSuccess(Void aVoid) {
						placeChanger.goTo(new Profile(authController.getCurrentUserPrincipalId(), Synapse.ProfileArea.TEAMS));
					}
				});
			}
		});
	}

	private void initializeView(final MembershipInvtnSubmission mis) {
		view.show();
		registerWidget.enableEmailAddressField(false);
		view.setRegisterWidget(registerWidget.asWidget());
		view.setSynapseAlertContainer(synapseAlert.asWidget());
		EmailInvitationPresenter.this.registerWidget.setEncodedMembershipInvtnSignedToken(encodedMembershipInvtnSignedToken);
		EmailInvitationPresenter.this.registerWidget.setEmail(mis.getInviteeEmail());
		jsClient.getTeam(mis.getTeamId(), new AsyncCallback<Team>() {
			@Override
			public void onFailure(Throwable throwable) {
				synapseAlert.handleException(throwable);
			}

			@Override
			public void onSuccess(final Team team) {
				jsClient.getUserProfile(mis.getCreatedBy(), new AsyncCallback<UserProfile>() {
					@Override
					public void onFailure(Throwable throwable) {
						synapseAlert.handleException(throwable);
					}

					@Override
					public void onSuccess(UserProfile userProfile) {
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
				});
			}
		});
	}

	@Override
	public void onLoginClick() {
		placeChanger.goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
	}
}
