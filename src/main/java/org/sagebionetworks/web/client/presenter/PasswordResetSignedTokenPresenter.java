package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.auth.ChangePasswordWithToken;
import org.sagebionetworks.repo.model.auth.PasswordResetSignedToken;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.PasswordResetSignedTokenPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.PasswordResetSignedTokenView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class PasswordResetSignedTokenPresenter extends AbstractActivity implements PasswordResetSignedTokenView.Presenter, Presenter<PasswordResetSignedTokenPlace> {
	public static final String INVALID_PASSWORD_RESET_SIGNED_TOKEN = "Invalid PasswordResetSignedToken";
	private PasswordResetSignedTokenView view;
	private SynapseClientAsync synapseClient;
	private SynapseJavascriptClient jsClient;
	private SynapseAlert synAlert;
	PasswordResetSignedToken signedToken;
	AuthenticationController authController;
	GlobalApplicationState globalAppState;

	@Inject
	public PasswordResetSignedTokenPresenter(PasswordResetSignedTokenView view, SynapseClientAsync synapseClient, SynapseJavascriptClient jsClient, SynapseAlert synAlert, AuthenticationController authController, GlobalApplicationState globalAppState) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.authController = authController;
		this.globalAppState = globalAppState;
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(PasswordResetSignedTokenPlace place) {
		view.setPresenter(this);
		configure(place.getToken());
	}

	public void configure(final String signedEncodedToken) {
		signedToken = null;
		synAlert.clear();
		view.clear();
		// hex decode the token
		synapseClient.hexDecodeAndDeserialize(signedEncodedToken, new AsyncCallback<SignedTokenInterface>() {
			@Override
			public void onSuccess(SignedTokenInterface result) {
				if (!(result instanceof PasswordResetSignedToken)) {
					synAlert.showError(INVALID_PASSWORD_RESET_SIGNED_TOKEN);
					return;
				}
				signedToken = (PasswordResetSignedToken) result;

			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	private boolean checkPasswordDefined(String password) {
		return password != null && !password.isEmpty();
	}

	@Override
	public void onChangePassword() {
		synAlert.clear();
		String password1 = view.getPassword1Field();
		String password2 = view.getPassword2Field();
		if (!checkPasswordDefined(password1) || !checkPasswordDefined(password2)) {
			synAlert.showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
		} else if (!password1.equals(password2)) {
			synAlert.showError(DisplayConstants.PASSWORDS_MISMATCH);
		} else {
			view.setChangePasswordEnabled(false);
			ChangePasswordWithToken changePasswordRequest = new ChangePasswordWithToken();
			changePasswordRequest.setNewPassword(password1);
			changePasswordRequest.setPasswordChangeToken(signedToken);
			jsClient.changePassword(changePasswordRequest, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					view.setChangePasswordEnabled(true);
					synAlert.handleException(caught);
				}

				@Override
				public void onSuccess(Void result) {
					view.showPasswordChangeSuccess();
					view.setChangePasswordEnabled(true);
					globalAppState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
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
