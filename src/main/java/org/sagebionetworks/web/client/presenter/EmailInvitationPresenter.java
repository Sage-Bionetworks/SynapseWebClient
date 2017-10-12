package org.sagebionetworks.web.client.presenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.sagebionetworks.repo.model.InviteeVerificationSignedToken;
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

import javax.inject.Inject;

public class EmailInvitationPresenter implements EmailInvitationView.Presenter, Presenter<EmailInvitation> {
	private String invitationId;
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
		view.setRegisterWidget(registerWidget.asWidget());
		view.setSynapseAlertContainer(synapseAlert.asWidget());
	}

	@Override
	public void setPlace(EmailInvitation place) {
		this.invitationId = place.toToken();
		if (authController.isLoggedIn()) {
			synapseClient.getInviteeVerificationSignedToken(invitationId, new AsyncCallback<InviteeVerificationSignedToken>() {
				@Override
				public void onFailure(Throwable throwable) {
					synapseAlert.handleException(throwable);
				}

				@Override
				public void onSuccess(InviteeVerificationSignedToken inviteeVerificationSignedToken) {
					updateInviteeId(inviteeVerificationSignedToken);
				}
			});
		} else {
			placeChanger.goTo(new LoginPlace(null));
		}
	}

	private void updateInviteeId(InviteeVerificationSignedToken token) {
		synapseClient.updateInviteeId(invitationId, token, new AsyncCallback<Void>() {
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

	@Override
	public String mayStop() {
		return null;
	}

	@Override
	public void onCancel() {

	}

	@Override
	public void onStop() {

	}

	@Override
	public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
		acceptsOneWidget.setWidget(view);
	}
}
