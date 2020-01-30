package org.sagebionetworks.web.client.presenter.users;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.Presenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class RegisterAccountPresenter extends AbstractActivity implements RegisterAccountView.Presenter, Presenter<RegisterAccount> {
	private RegisterAccount place;
	private RegisterAccountView view;

	private RegisterWidget registerWidget;
	private AuthenticationController authController;
	private GlobalApplicationState globalAppState;
	SynapseAlert googleSynAlert;
	private SynapseClientAsync synapseClient;

	@Inject
	public RegisterAccountPresenter(RegisterAccountView view, SynapseClientAsync synapseClient, RegisterWidget registerWidget, AuthenticationController authController, GlobalApplicationState globalAppState, SynapseAlert googleSynAlert) {
		this.view = view;
		this.registerWidget = registerWidget;
		this.authController = authController;
		this.globalAppState = globalAppState;
		this.synapseClient = synapseClient;
		this.googleSynAlert = googleSynAlert;
		fixServiceEntryPoint(synapseClient);
		view.setRegisterWidget(registerWidget.asWidget());
		view.setGoogleSynAlert(googleSynAlert);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(RegisterAccount place) {
		this.place = place;
		String token = place.toToken();
		String email = "";
		if (token != null && !ClientProperties.DEFAULT_PLACE_TOKEN.equals(token)) {
			email = token.trim();
		}
		registerWidget.setEmail(email);
		view.setPresenter(this);
		if (authController.isLoggedIn()) {
			// SWC-4363
			globalAppState.getPlaceChanger().goTo(new Profile(Profile.VIEW_PROFILE_TOKEN));
		}
	}

	/**
	 * Check that the username/alias is available
	 * 
	 * @param username
	 */
	public void checkUsernameAvailable(String username) {
		googleSynAlert.clear();
		synapseClient.isAliasAvailable(username, AliasType.USER_NAME.toString(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean isAvailable) {
				if (!isAvailable) {
					googleSynAlert.showError(DisplayConstants.ERROR_USERNAME_ALREADY_EXISTS);
				} else {
					view.setGoogleRegisterButtonEnabled(true);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				googleSynAlert.handleException(e);
			}
		});
	}
}
