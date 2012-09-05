package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.TermsOfUseException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class LoginPresenter extends AbstractActivity implements LoginView.Presenter {

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
	
	@Inject
	public LoginPresenter(LoginView view, AuthenticationController authenticationController, UserAccountServiceAsync userService, GlobalApplicationState globalApplicationState, NodeModelCreator nodeModelCreator, CookieProvider cookies){
		this.view = view;
		this.authenticationController = authenticationController;
		this.userService = userService;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator = nodeModelCreator;
		this.cookies = cookies;
		view.setPresenter(this);
	} 

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(this.view.asWidget());
		this.bus = eventBus;
		
	}
	
	public static final String LOGIN_PLACE  = "LoginPlace";

	public void setPlace(final LoginPlace place) {
		this.loginPlace = place;
		view.setPresenter(this);
		view.clear();
		if(openIdActionUrl != null && openIdReturnUrl != null) {
			showView(place);
		} else {
			// load Open ID urls
			// retrieve endpoints for SSO
			userService.getPublicAuthServiceUrl(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					openIdActionUrl = result + "/openid";
					
					userService.getSynapseWebUrl(new AsyncCallback<String>() {
						@Override
						public void onSuccess(String result) {
							// this should be a string as the Auth service completes the URL with ":<sessionId>"
							openIdReturnUrl = result + "/#"+LOGIN_PLACE;
							
							// now show the view
							showView(place);
						}
						@Override
						public void onFailure(Throwable caught) {
							DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());
						}
					});					
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
						view.showErrorMessage("An Error occurred. Please try reloading the page.");
					}
				}
			});
		}
	}

	private void showView(final LoginPlace place) {
		String token = place.toToken();
		if (LoginPlace.FASTPASS_TOKEN.equals(token)) {
			//fastpass!
			
			//is the user already logged in?  
			UserSessionData currentUser = authenticationController.getLoggedInUser();
			if(currentUser != null){
				//the user might be logged in.  verify the session token
				authenticationController.loginUser(currentUser.getSessionToken(), new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						//success, go to support the support site
						gotoSupport();
					}
					@Override
					public void onFailure(Throwable caught) {
						//not really logged in. 
						authenticationController.logoutUser();
						showView(place);
					}
				});
				return;
			}
			else { 
				//not logged in. do normal login, but after all is done, redirect back to the support site
				cookies.setCookie(DisplayUtils.FASTPASS_LOGIN_COOKIE_VALUE, Boolean.TRUE.toString());
				token = DisplayUtils.DEFAULT_PLACE_TOKEN;
			}
			
		}
		
		if(LoginPlace.LOGOUT_TOKEN.equals(token)) {
			UserSessionData currentUser = authenticationController.getLoggedInUser(); 
			boolean isSso = false;
			if(currentUser != null)
				isSso = currentUser.getIsSSO();
			authenticationController.logoutUser();
			view.showLogout(isSso);
		} else if (token!=null && ServiceConstants.ACCEPTS_TERMS_OF_USE_REQUIRED_TOKEN.equals(token)) {
			userService.getTermsOfUse(new AsyncCallback<String>() {
				public void onSuccess(String content) {
					view.showTermsOfUse(content, 
						new AcceptTermsOfUseCallback() {
							public void accepted() {
								view.acceptTermsOfUse();
								globalApplicationState.getPlaceChanger().goTo(new LoginPlace(DisplayUtils.DEFAULT_PLACE_TOKEN));
							} 
						});			
				}
				public void onFailure(Throwable throwable) {
					view.showErrorMessage("An error occured. Please try logging in again.");
					view.showLogin(openIdActionUrl, openIdReturnUrl);
				}
			});
		} else if (!DisplayUtils.DEFAULT_PLACE_TOKEN.equals(token)				
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

						if (caught instanceof TermsOfUseException) {
							authenticationController.getTermsOfUse(new AsyncCallback<String>() {
								public void onSuccess(String termsOfUseContents) {
									view.showTermsOfUse(termsOfUseContents, 
											new AcceptTermsOfUseCallback() {
												public void accepted() {showView(place);}
											});		
								}
								public void onFailure(Throwable t) {
									view.showErrorMessage("An error occured. Please try logging in again.");
									view.showLogin(openIdActionUrl, openIdReturnUrl);									
								}
							});
						} else {
							view.showErrorMessage("An error occured. Please try logging in again.");
							view.showLogin(openIdActionUrl, openIdReturnUrl);
						}
					}
				});
			} 
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
		String isFastPassLogin = cookies.getCookie(DisplayUtils.FASTPASS_LOGIN_COOKIE_VALUE);
		if (isFastPassLogin != null && Boolean.valueOf(isFastPassLogin)){
			cookies.removeCookie(DisplayUtils.FASTPASS_LOGIN_COOKIE_VALUE);
			gotoSupport();
		}
		else {
			if(forwardPlace == null) {
				forwardPlace = new Home(DisplayUtils.DEFAULT_PLACE_TOKEN);
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
						Window.Location.replace("http://support.sagebase.org/fastpass/finish_signover?company=sagebase&fastpass="+URL.encodeQueryString(result));
					else
						//can't go on, just fail
						view.showErrorMessage(DisplayConstants.ERROR_NO_FASTPASS);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.ERROR_NO_FASTPASS + caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(DisplayConstants.ERROR_NO_FASTPASS + e.getMessage());

		}
	}
}
