package org.sagebionetworks.web.client.presenter;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.NewAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.NewAccountView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class NewAccountPresenter extends AbstractActivity implements NewAccountView.Presenter, Presenter<NewAccount> {
		
	public static final String EMAIL_KEY = "email";
	private NewAccount place;
	private NewAccountView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private UserAccountServiceAsync userAccountService;
	private AuthenticationController authController;
	private GWTWrapper gwt;
	private String emailValidationToken;
	private Map<String, String> emailValidationTokenParams;
	
	@Inject
	public NewAccountPresenter(NewAccountView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalAppState,
			UserAccountServiceAsync userAccountService,
			AuthenticationController authController,
			GWTWrapper gwt){
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.userAccountService = userAccountService;
		this.authController = authController;
		this.gwt = gwt;
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
		emailValidationToken = place.getFixedToken();
		emailValidationTokenParams = parseEmailValidationToken(emailValidationToken);
		String email = emailValidationTokenParams.get(EMAIL_KEY);
		view.setEmail(email);
		checkEmailAvailable(email);
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
		userAccountService.createUserStep2(userName, fName, lName, password, emailValidationToken, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String sessionToken) {
				//success, send to login place to continue login process (sign terms of use...)
				view.showInfo(DisplayConstants.ACCOUNT_CREATED, "");
				globalAppState.getPlaceChanger().goTo(new LoginPlace(sessionToken));
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ACCOUNT_CREATION_FAILURE + caught.getMessage());
			}
		});
	}
	

	/**
	 * check that the email is available
	 * @param username
	 * @param email
	 * @param firstName
	 * @param lastName
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
	
	/**
	 * Expose for testing purposes only 
	 * @param emailValidationToken
	 */
	public void setEmailValidationToken(String emailValidationToken) {
		this.emailValidationToken = emailValidationToken;
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
