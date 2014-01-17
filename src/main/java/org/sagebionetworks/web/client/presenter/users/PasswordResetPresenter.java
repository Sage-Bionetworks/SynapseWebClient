package org.sagebionetworks.web.client.presenter.users;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.Presenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.users.PasswordResetView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

@SuppressWarnings("unused")
public class PasswordResetPresenter extends AbstractActivity implements PasswordResetView.Presenter, Presenter<PasswordReset> {
	private PasswordReset place;	
	private PasswordResetView view;
	private CookieProvider cookieProvider;
	private UserAccountServiceAsync userService;
	private AuthenticationController authenticationController;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	
	private String sessionToken = null;
	
	@Inject
	public PasswordResetPresenter(PasswordResetView view,
			CookieProvider cookieProvider, UserAccountServiceAsync userService,
			AuthenticationController authenticationController,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.userService = userService;
		this.authenticationController = authenticationController;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		// Set the presenter on the view
		this.cookieProvider = cookieProvider;
		this.globalApplicationState = globalApplicationState;
		
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(PasswordReset place) {
		this.place = place;
		view.setPresenter(this);			
		view.clear(); 
		
		// Assume all tokens other than the default are session tokens
		if (!ClientProperties.DEFAULT_PLACE_TOKEN.equals(place.toToken())) {
			sessionToken = place.toToken();
			// validate that session token is still valid before showing form
			view.showLoading();
			authenticationController.loginUser(sessionToken, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					view.showResetForm();	
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showExpiredRequest();
				}
			});
		} else {
			view.showRequestForm();
		}
	}

	@Override
	public void requestPasswordReset(String emailAddress) {
		userService.sendPasswordResetEmail(emailAddress, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showRequestSentSuccess();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage("An error occurred in sending your request. Please retry.");
			}
		});
		
	}

	@Override
	public void resetPassword(final String newPassword) {
		if (sessionToken == null && authenticationController.isLoggedIn()) {
			sessionToken = authenticationController.getCurrentUserSessionToken();
		}
		userService.changePassword(sessionToken, newPassword, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo("", DisplayConstants.PASSWORD_RESET_TEXT);
				view.showPasswordResetSuccess();
				if (authenticationController.getCurrentUserSessionData().getSession().getAcceptsTermsOfUse())
					globalApplicationState.getPlaceChanger().goTo(new Home(ClientProperties.DEFAULT_PLACE_TOKEN)); // redirect to home page
				else {
					//pop up terms of service on re-login
					authenticationController.logoutUser();
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN)); // redirect to login page
				}
					
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.PASSWORD_RESET_FAILED_TEXT);
			}
		});
	}
}
