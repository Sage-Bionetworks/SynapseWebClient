package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.client.*;
import org.sagebionetworks.web.client.place.EmailInvitation;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.NewAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.NewAccountView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.login.PasswordStrengthWidget;

import java.util.HashMap;
import java.util.Map;

public class NewAccountPresenter extends AbstractActivity implements NewAccountView.Presenter, Presenter<NewAccount> {
		
	public static final String EMAIL_KEY = "email";
	private NewAccount place;
	private NewAccountView view;
	private SynapseClientAsync synapseClient;
	private SynapseAlert synapseAlert;
	private GlobalApplicationState globalAppState;
	private UserAccountServiceAsync userAccountService;
	private AuthenticationController authController;
	private GWTWrapper gwt;
	PasswordStrengthWidget passwordStrengthWidget;
	private String emailValidationToken;
	private Map<String, String> emailValidationTokenParams;
	private AccountCreationToken accountCreationToken;

	@Inject
	public NewAccountPresenter(NewAccountView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalAppState,
			UserAccountServiceAsync userAccountService,
			AuthenticationController authController,
			GWTWrapper gwt,
			PasswordStrengthWidget passwordStrengthWidget){
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.userAccountService = userAccountService;
		this.authController = authController;
		this.gwt = gwt;
		this.passwordStrengthWidget = passwordStrengthWidget;
		view.setPasswordStrengthWidget(passwordStrengthWidget.asWidget());
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(NewAccount place) {
		authController.logoutUser();
		this.place = place;
		view.clear();
		this.view.setPresenter(this);

		if (place.toToken().contains("email")) {
			// Handle old style email validation token
			emailValidationToken = place.toToken();
			//SWC-3222: if token is encoded, then decode before parsing.
			if (emailValidationToken != null && emailValidationToken.contains("&amp;")) {
				emailValidationToken = gwt.decodeQueryString(emailValidationToken);
			}
			emailValidationTokenParams = parseEmailValidationToken(emailValidationToken);
			String email = emailValidationTokenParams.get(EMAIL_KEY);
			view.setEmail(email);
			checkEmailAvailable(email);
		} else {
			// Handle AccountCreationToken
			synapseClient.hexDecodeAndDeserializeAccountCreationToken(place.toToken(), new AsyncCallback<AccountCreationToken>() {
				@Override
				public void onSuccess(AccountCreationToken result) {
					accountCreationToken = result;
					if (!accountCreationTokenIsValid()) {
						view.setLoading(false);
						view.showErrorMessage(DisplayConstants.ACCOUNT_CREATION_FAILURE + " token is not valid");
						return;
					}
					String email = accountCreationToken.getEmailValidationSignedToken().getEmail();
					view.setEmail(email);
					checkEmailAvailable(email);
				}

				@Override
				public void onFailure(Throwable caught) {
					view.setLoading(false);
					view.showErrorMessage(DisplayConstants.ACCOUNT_CREATION_FAILURE + caught.getMessage());
				}
			});
		}
	}

	private boolean accountCreationTokenIsValid() {
		return accountCreationToken.getEmailValidationSignedToken() != null;
	}

	public Map<String, String> parseEmailValidationToken(String token) {
		Map<String, String> tokenMap = new HashMap<String, String>();
		if (token != null) {
			String[] keyValues = token.split("&");
			for (String keyValue : keyValues) {
				String[] keyAndValue = keyValue.split("=");
				if (keyAndValue.length == 2) {
					tokenMap.put(keyAndValue[0].toLowerCase(), gwt.decodeQueryString(keyAndValue[1]));
				}
			}
		}
		return tokenMap;
	}

	@Override
	public void completeRegistration(String userName, String fName, String lName, String password) {
		view.setLoading(true);
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String sessionToken) {
				view.setLoading(false);
				//success, send to login place to continue login process (sign terms of use...)
				view.showInfo(DisplayConstants.ACCOUNT_CREATED, "");
				if (accountCreationToken.getEncodedMembershipInvtnSignedToken() != null) {
					globalAppState.setLastPlace(new EmailInvitation(accountCreationToken.getEncodedMembershipInvtnSignedToken()));
				}
				globalAppState.getPlaceChanger().goTo(new LoginPlace(sessionToken));
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setLoading(false);
				view.showErrorMessage(DisplayConstants.ACCOUNT_CREATION_FAILURE + caught.getMessage());
			}
		};
		if (emailValidationToken != null && accountCreationToken == null) {
			userAccountService.createUserStep2(userName.trim(), fName.trim(), lName.trim(), password, emailValidationToken, callback);
		} else if (emailValidationToken == null && accountCreationToken != null) {
			EmailValidationSignedToken emailValidationSignedToken = accountCreationToken.getEmailValidationSignedToken();
			userAccountService.createUserStep2(userName.trim(), fName.trim(), lName.trim(), password, emailValidationSignedToken, callback);
		} else {
			view.setLoading(false);
			view.showErrorMessage(DisplayConstants.ACCOUNT_CREATION_FAILURE + "One and only one type of email validation token must be provided.");
		}
	}
	

	/**
	 * check that the email is available
	 * @param email
	 */
	public void checkEmailAvailable(String email) {
		synapseClient.isAliasAvailable(email, AliasType.USER_EMAIL.toString(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean isAvailable) {
				if (!isAvailable) {
					view.showErrorMessage(DisplayConstants.ERROR_EMAIL_ALREADY_EXISTS);
					globalAppState.gotoLastPlace();
				}
			}
			
			@Override
			public void onFailure(Throwable e) {
				//do nothing.  validation has failed, but updating the email will fail if it's already taken.
				e.printStackTrace();
			}
		});
	}

	/**
	 * Check that the username/alias is available
	 * @param username
	 */
	public void checkUsernameAvailable(String username) {
		if (username.trim().length() > 3) {
			synapseClient.isAliasAvailable(username, AliasType.USER_NAME.toString(), new AsyncCallback<Boolean>() {
				@Override
				public void onSuccess(Boolean isAvailable) {
					if (!isAvailable)
						view.markUsernameUnavailable();
				}
				
				@Override
				public void onFailure(Throwable e) {
					//do nothing.  validation has failed, but updating the username will fail if it's already taken.
					e.printStackTrace();
				}
			});
		}
	}
	
	public String getEmailValidationToken() {
		return emailValidationToken;
	}
	
	@Override
	public void passwordChanged(String password) {
		passwordStrengthWidget.scorePassword(password);
	}
	
	@Override
	public String mayStop() {
		view.clear();
		return null;
	}
}
