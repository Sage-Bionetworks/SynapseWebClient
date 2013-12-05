package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class LoginPresenter extends AbstractActivity implements LoginView.Presenter, Presenter<LoginPlace> {

	private LoginPlace loginPlace;
	private LoginView view;
	private EventBus bus;
	private AuthenticationController authenticationController;
	private String openIdActionUrl;
	private String openIdReturnUrl;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private CookieProvider cookies;
	private GWTWrapper gwtWrapper;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public LoginPresenter(LoginView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, NodeModelCreator nodeModelCreator, CookieProvider cookies, GWTWrapper gwtWrapper, SynapseJSNIUtils synapseJSNIUtils){
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator = nodeModelCreator;
		this.cookies = cookies;
		this.gwtWrapper = gwtWrapper;
		this.synapseJSNIUtils=synapseJSNIUtils;
		view.setPresenter(this);
	} 

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(this.view.asWidget());
		this.bus = eventBus;
		
	}
	
	public static final String LOGIN_PLACE  = "LoginPlace";

	@Override
	public void setPlace(final LoginPlace place) {
		this.loginPlace = place;
		view.setPresenter(this);
		view.clear();
		openIdActionUrl = WebConstants.OPEN_ID_URI;
		// note, this is now a relative URL
		openIdReturnUrl = synapseJSNIUtils.getLocationPath()+synapseJSNIUtils.getLocationQueryString()+"#!"+LOGIN_PLACE; 
		showView(place);
	}
	
	public String getOpenIdReturnUrl() {
		return openIdReturnUrl;
	}

	public void showView(final LoginPlace place) {
		String token = place.toToken();
		if(LoginPlace.LOGOUT_TOKEN.equals(token)) {			
			boolean isSso = false;
			if(authenticationController.isLoggedIn())
				isSso = authenticationController.getCurrentUserIsSSO();
			authenticationController.logoutUser();
			view.showLogout(isSso);
		} else if (WebConstants.OPEN_ID_UNKNOWN_USER_ERROR_TOKEN.equals(token)) {
			// User does not exist, redirect to Registration page
			view.showErrorMessage(DisplayConstants.CREATE_ACCOUNT_MESSAGE_SSO);
			globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));			
		} else if (WebConstants.OPEN_ID_ERROR_TOKEN.equals(token)) {
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
			view.showErrorMessage(DisplayConstants.SSO_ERROR_UNKNOWN);
			view.showLogin(openIdActionUrl, openIdReturnUrl);
		} else if (!ClientProperties.DEFAULT_PLACE_TOKEN.equals(token) && !"".equals(token) && token != null) {			
			loginSSOUser(token);
		} else {
			// standard view
			authenticationController.logoutUser();
			view.showLogin(openIdActionUrl, openIdReturnUrl);
		}
	}
	
	@Override
	public void setNewUser(UserSessionData newUser) {	
		// Allow the user to proceed.		
		forwardToPlaceAfterLogin(globalApplicationState.getLastPlace());		
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	/*
	 * Private Methods
	 */
	private void forwardToPlaceAfterLogin(Place forwardPlace) {
		if(forwardPlace == null) {
			forwardPlace = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
		}
		bus.fireEvent(new PlaceChangeEvent(forwardPlace));	
	}
	
	private void loginSSOUser(String token) {
		// Single Sign on token. try refreshing the token to see if it is valid. if so, log user in
		// parse token
		view.showLoggingInLoader();
		if(token != null) {
			final String sessionToken = token;	
			authenticationController.loginUserSSO(sessionToken, new AsyncCallback<String>() {	
				@Override
				public void onSuccess(String result) {
					
					// Show the ToU dialog if necessary
					if (!authenticationController.getCurrentUserSessionData().getSession().getAcceptsTermsOfUse()) {
						
						authenticationController.getTermsOfUse(new AsyncCallback<String>() {
							public void onSuccess(String termsOfUseContents) {
								view.hideLoggingInLoader();
								view.showTermsOfUse(termsOfUseContents, 
										new AcceptTermsOfUseCallback() {
											public void accepted() {
												view.showLoggingInLoader();
												authenticationController.signTermsOfUse(true, new AsyncCallback<Void> () {

													@Override
													public void onFailure(Throwable caught) {
														view.showErrorMessage("An error occurred. Please try logging in again.");
														view.showLogin(openIdActionUrl, openIdReturnUrl);
													}

													@Override
													public void onSuccess(Void result) {
														// Have to get the UserSessionData again, 
														// since it won't contain the UserProfile if the terms haven't been signed
														authenticationController.loginUserSSO(sessionToken, new AsyncCallback<String>() {

															@Override
															public void onFailure(
																	Throwable caught) {
																view.showErrorMessage("An error occurred. Please try logging in again.");
																view.showLogin(openIdActionUrl, openIdReturnUrl);
															}

															@Override
															public void onSuccess(
																	String result) {
																view.hideLoggingInLoader();
																// All setup complete, so forward the user
																forwardToPlaceAfterLogin(globalApplicationState.getLastPlace());
															}	
															
														});
													}
													
												});
											}

											@Override
											public void rejected() {
												authenticationController.signTermsOfUse(false, new AsyncCallback<Void> () {

													@Override
													public void onFailure(Throwable caught) {
														view.showErrorMessage("An error occurred. Please try logging in again.");
														view.showLogin(openIdActionUrl, openIdReturnUrl);
													}

													@Override
													public void onSuccess(Void result) {
														authenticationController.logoutUser();
														forwardToPlaceAfterLogin(globalApplicationState.getLastPlace());
													}
													
												});
											}
										});		
							}
							public void onFailure(Throwable t) {
								if(!DisplayUtils.checkForRepoDown(t, globalApplicationState.getPlaceChanger(), view)) 
									view.showErrorMessage("An error occurred. Please try logging in again.");
								view.showLogin(openIdActionUrl, openIdReturnUrl);									
							}
						});
					} else {
						view.hideLoggingInLoader();
						// user is logged in. forward to destination
						forwardToPlaceAfterLogin(globalApplicationState.getLastPlace());
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					if(DisplayUtils.checkForRepoDown(caught, globalApplicationState.getPlaceChanger(), view)) {
						view.showLogin(openIdActionUrl, openIdReturnUrl);
						return;
					}
					view.showErrorMessage("An error occurred. Please try logging in again.");
					view.showLogin(openIdActionUrl, openIdReturnUrl);
				}
			});
		}
	}

}
