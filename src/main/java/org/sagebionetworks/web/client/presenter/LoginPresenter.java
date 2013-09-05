package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.TermsOfUseException;

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
	private UserAccountServiceAsync userService;
	private String openIdActionUrl;
	private String openIdReturnUrl;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private CookieProvider cookies;
	private GWTWrapper gwtWrapper;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public LoginPresenter(LoginView view, AuthenticationController authenticationController, UserAccountServiceAsync userService, GlobalApplicationState globalApplicationState, NodeModelCreator nodeModelCreator, CookieProvider cookies, GWTWrapper gwtWrapper, SynapseJSNIUtils synapseJSNIUtils){
		this.view = view;
		this.authenticationController = authenticationController;
		this.userService = userService;
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

	@SuppressWarnings("deprecation")
	public void showView(final LoginPlace place) {
		String token = place.toToken();
		if (LoginPlace.FASTPASS_TOKEN.equals(token)) {
			//fastpass!
			
			//is the user already logged in?  			
			if(authenticationController.isLoggedIn()){
				//the user might be logged in.  verify the session token
				authenticationController.loginUser(authenticationController.getCurrentUserSessionToken(), new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						//success, go to support the support site
						gotoSupport();
					}
					@Override
					public void onFailure(Throwable caught) {
						if(DisplayUtils.checkForRepoDown(caught, globalApplicationState.getPlaceChanger(), view)) {
							//not really logged in. 
							authenticationController.logoutUser();
							return;
						}
						//not really logged in. 
						authenticationController.logoutUser();
						showView(place);
					}
				});
				return;
			}
			else { 
				//not logged in. do normal login, but after all is done, redirect back to the support site
				cookies.setCookie(ClientProperties.FASTPASS_LOGIN_COOKIE_VALUE, Boolean.TRUE.toString());
				token = ClientProperties.DEFAULT_PLACE_TOKEN;
			}
			
		}
		
		if(LoginPlace.LOGOUT_TOKEN.equals(token)) {			
			boolean isSso = false;
			if(authenticationController.isLoggedIn())
				isSso = authenticationController.getCurrentUserIsSSO();
			authenticationController.logoutUser();
			view.showLogout(isSso);
		} else if (token!=null && WebConstants.ACCEPTS_TERMS_OF_USE_REQUIRED_TOKEN.equals(token)) {
			userService.getTermsOfUse(new AsyncCallback<String>() {
				public void onSuccess(String content) {
					view.showTermsOfUse(content, 
						new AcceptTermsOfUseCallback() {
							public void accepted() {
								view.acceptTermsOfUse();
								globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
							} 
						});			
				}
				public void onFailure(Throwable throwable) {
					if(!DisplayUtils.checkForRepoDown(throwable, globalApplicationState.getPlaceChanger(), view))
						view.showErrorMessage("An error occurred. Please try logging in again.");
					view.showLogin(openIdActionUrl, openIdReturnUrl);				}
			});
		} else if (!ClientProperties.DEFAULT_PLACE_TOKEN.equals(token)				
				&& !"".equals(token) && token != null) {			
			// Single Sign on token. try refreshing the token to see if it is valid. if so, log user in
			// parse token
			view.showLoggingInLoader();
			if(token != null) {
				String sessionToken = token;	
				authenticationController.loginUserSSO(sessionToken, new AsyncCallback<String>() {	
					@Override
					public void onSuccess(String result) {
						view.hideLoggingInLoader();
						// user is logged in. forward to destination						
						forwardToPlaceAfterLogin(globalApplicationState.getLastPlace());
					}
					@Override
					public void onFailure(Throwable caught) {
						if(DisplayUtils.checkForRepoDown(caught, globalApplicationState.getPlaceChanger(), view)) {
							view.showLogin(openIdActionUrl, openIdReturnUrl);
							return;
						}
						if (caught instanceof TermsOfUseException) {
							authenticationController.getTermsOfUse(new AsyncCallback<String>() {
								public void onSuccess(String termsOfUseContents) {
									view.showTermsOfUse(termsOfUseContents, 
											new AcceptTermsOfUseCallback() {
												public void accepted() {showView(place);}
											});		
								}
								public void onFailure(Throwable t) {
									if(!DisplayUtils.checkForRepoDown(t, globalApplicationState.getPlaceChanger(), view)) 
										view.showErrorMessage("An error occurred. Please try logging in again.");
									view.showLogin(openIdActionUrl, openIdReturnUrl);									
								}
							});
						} else {
							view.showErrorMessage("An error occurred. Please try logging in again.");
							view.showLogin(openIdActionUrl, openIdReturnUrl);
						}
					}
				});
			} 
		} else if (WebConstants.OPEN_ID_ERROR_TOKEN.equals(token)) {
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
			view.showErrorMessage("An error occurred. Please try logging in again.");
			view.showLogin(openIdActionUrl, openIdReturnUrl);
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

	
	/*
	 * Private Methods
	 */
	private void forwardToPlaceAfterLogin(Place forwardPlace) {
		String isFastPassLogin = cookies.getCookie(ClientProperties.FASTPASS_LOGIN_COOKIE_VALUE);
		if (isFastPassLogin != null && Boolean.valueOf(isFastPassLogin)){
			cookies.removeCookie(ClientProperties.FASTPASS_LOGIN_COOKIE_VALUE);
			gotoSupport();
		}
		else {
			if(forwardPlace == null) {
				forwardPlace = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
			}
			bus.fireEvent(new PlaceChangeEvent(forwardPlace));	
		}
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	public void gotoSupport() {
		try {
			userService.getFastPassSupportUrl(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					if (result != null && result.length()>0)
						//send user to "http://support.sagebase.org/fastpass/finish_signover?company=sagebase&fastpass="+URL.encodeQueryString(result)
						gwtWrapper.replaceThisWindowWith(ClientProperties.FASTPASS_SIGNOVER_URL + gwtWrapper.encodeQueryString(result));
					else
						//can't go on, just fail
						view.showErrorMessage(DisplayConstants.ERROR_NO_FASTPASS);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(DisplayUtils.checkForRepoDown(caught, globalApplicationState.getPlaceChanger(), view)) return;
					view.showErrorMessage(DisplayConstants.ERROR_NO_FASTPASS + caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(DisplayConstants.ERROR_NO_FASTPASS + e.getMessage());

		}
	}


}
