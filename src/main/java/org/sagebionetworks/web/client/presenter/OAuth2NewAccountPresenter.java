package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.OAuth2NewAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.OAuth2NewAccountView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class OAuth2NewAccountPresenter extends AbstractActivity implements OAuth2NewAccountView.Presenter, Presenter<OAuth2NewAccount> {
	public static final String ERROR_PLACE_PARAM = "error";
	private OAuth2NewAccountView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private AuthenticationController authController;
	private SynapseAlert synAlert;

	@Inject
	public OAuth2NewAccountPresenter(OAuth2NewAccountView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalAppState,
			AuthenticationController authController,
			SynapseAlert synAlert){
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		fixServiceEntryPoint(synapseClient);
		this.globalAppState = globalAppState;
		this.authController = authController;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(OAuth2NewAccount place) {
		if (authController.isLoggedIn()) {
			globalAppState.clearLastPlace();
			authController.logoutUser();
		}
		view.clear();
		synAlert.clear();
		this.view.setPresenter(this);
		String error = place.getParam(ERROR_PLACE_PARAM);
		if (error != null && !error.isEmpty()) {
			synAlert.showError(error);
		}
	}

	/**
	 * Check that the username/alias is available
	 * @param username
	 */
	public void checkUsernameAvailable(String username) {
		synAlert.clear();
		if (username.trim().length() > 3) {
			synapseClient.isAliasAvailable(username, AliasType.USER_NAME.toString(), new AsyncCallback<Boolean>() {
				@Override
				public void onSuccess(Boolean isAvailable) {
					if (!isAvailable) {
						synAlert.showError(DisplayConstants.ERROR_USERNAME_ALREADY_EXISTS);
					}
				}
				
				@Override
				public void onFailure(Throwable e) {
					synAlert.handleException(e);
				}
			});
		}
	}
	
	@Override
	public String mayStop() {
		view.clear();
		return null;
	}
}
