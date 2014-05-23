package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
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
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
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
	private SynapseJSNIUtils synapseJSNIUtils;
	private JSONObjectAdapter jsonObjectAdapter;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private CookieProvider cookies;
	private UserProfile profile;
	
	@Inject
	public LoginPresenter(LoginView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, NodeModelCreator nodeModelCreator, CookieProvider cookies, GWTWrapper gwtWrapper, SynapseJSNIUtils synapseJSNIUtils, JSONObjectAdapter jsonObjectAdapter, SynapseClientAsync synapseClient, AdapterFactory adapterFactory){
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseJSNIUtils=synapseJSNIUtils;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.cookies = cookies;
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
			authenticationController.logoutUser();
			view.showLogout();
		} else if (WebConstants.OPEN_ID_UNKNOWN_USER_ERROR_TOKEN.equals(token)) {
			// User does not exist, redirect to Registration page
			view.showErrorMessage(DisplayConstants.CREATE_ACCOUNT_MESSAGE_SSO);
			globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));			
		} else if (WebConstants.OPEN_ID_ERROR_TOKEN.equals(token)) {
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
			view.showErrorMessage(DisplayConstants.SSO_ERROR_UNKNOWN);
			view.showLogin(openIdActionUrl, openIdReturnUrl);
		} else if (LoginPlace.CHANGE_USERNAME.equals(token) && authenticationController.isLoggedIn()) {
			//get the current profile, and set the view to set username
			ProfileFormWidget.getMyProfile(synapseClient, adapterFactory, new AsyncCallback<UserProfile>() {
				@Override
				public void onSuccess(UserProfile result) {
					profile = result;
					view.showSetUsernameUI();
				}
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				};
			});
		} else if (!ClientProperties.DEFAULT_PLACE_TOKEN.equals(token) && 
				!LoginPlace.CHANGE_USERNAME.equals(token) && 
				!"".equals(token) && 
				token != null) {			
			revalidateSession(token);
		} else {
			// standard view
			authenticationController.logoutUser();
			view.showLogin(openIdActionUrl, openIdReturnUrl);
		}
	}
	
	@Override
	public void setUsername(String newUsername) {
		if (profile != null) {
			//quick check to see if it's valid.
			if (isValidUsername(newUsername)) {
				profile.setUserName(newUsername);
				
				AsyncCallback<Void> profileUpdatedCallback = new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						view.showInfo("Successfully updated your username", "");
						postLoginStep2();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						view.showUsernameTaken();
					}
				};
				updateProfile(profile, profileUpdatedCallback);
			} else {
				//invalid username
				view.showUsernameInvalid();
			}
		}
	}
	
	public static boolean isValidUsername(String username) {
		if (username == null) return false;
		RegExp regEx = RegExp.compile(WebConstants.VALID_USERNAME_REGEX, "gm");
		MatchResult matchResult = regEx.exec(username);
		//the entire string must match (group 0 is the whole matched string)
		return (matchResult != null && username.equals(matchResult.getGroup(0))); 
	}
	
	public static boolean isValidEmail(String email) {
		if (email == null) return false;
		RegExp regEx = RegExp.compile(WebConstants.VALID_EMAIL_REGEX, "gm");
		MatchResult matchResult = regEx.exec(email);
		//the entire string must match (group 0 is the whole matched string)
		return (matchResult != null && email.equals(matchResult.getGroup(0))); 
	}
	
	public static boolean isValidUrl(String url, boolean isUndefinedUrlValid) {
		if (url == null || url.trim().length() == 0) {
			//url is undefined
			return isUndefinedUrlValid;
		}
		RegExp regEx = RegExp.compile(WebConstants.VALID_URL_REGEX, "gm");
		MatchResult matchResult = regEx.exec(url);
		return (matchResult != null && url.equals(matchResult.getGroup(0))); 
	}
	
	@Override
	public void setNewUser(UserSessionData newUser) {
		revalidateSession(newUser.getSession().getSessionToken());
	}
	
	/**
	 * Check for temp username, and prompt for change if user has not set
	 */
	public void postLoginStep1(){
		//get my profile, and check for a default username
		view.showLoggingInLoader();
		ProfileFormWidget.getMyProfile(synapseClient, adapterFactory, new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile result) {
				view.hideLoggingInLoader();
				profile = result;
				authenticationController.updateCachedProfile(profile);
				if (profile != null && DisplayUtils.isTemporaryUsername(profile.getUserName())) {
					//set your username
					view.showSetUsernameUI();
				}
				else {
					postLoginStep2();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//could not determine
				postLoginStep2();
			}
		});
	}
	
	/**
	 * Check to see if user is certified
	 */
	public void postLoginStep2(){
		view.showLoggingInLoader();
		if (!isHideQuizReminder()) {
			synapseClient.getCertifiedUserPassingRecord(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String passingRecordJson) {
					goToLastPlace();
				}
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof NotFoundException) {
						view.hideLoggingInLoader();
						view.showQuizInfoUI();
					} else
						goToLastPlace();
				}
			});
		} else {
			//don't check, user previously indicated to ignore quiz
			goToLastPlace();
		}
	}
	
	public boolean isHideQuizReminder() {
		//TODO: currently only in test website until tutorial content is ready
		if (DisplayUtils.isInTestWebsite(cookies)) {
//			if (profile != null) {
//				if (profile.getPreferences() != null) {
//					Boolean hideQuizReminder = profile.getPreferences().getDontShowCertifiedUserReminder();
//					return hideQuizReminder != null ? hideQuizReminder : false;
//				} else {
//					//if the preferences have not been set, then show the certification reminder
//					return false;
//				}
//			} else
			return false;
		} else {
			return true;
		}
	}

	
	@Override
	public void setHideQuizReminder(boolean hideQuizReminder) {
		//update profile if hiding
//		if (hideQuizReminder) {
//			if (profile.getPreferences() == null) 
//				profile.setPreferences(new UserPreferences());
//			UserPreferences notificationSettings = profile.getPreferences();
//			notificationSettings.setDontShowCertifiedUserReminder(hideQuizReminder);
//			AsyncCallback<Void> profileUpdatedCallback = new AsyncCallback<Void>() {
//				@Override
//				public void onSuccess(Void result) {
//					view.showInfo("Successfully updated your preferences", "");
//				}
//				
//				@Override
//				public void onFailure(Throwable caught) {
//					view.showErrorMessage(caught.getMessage());
//				}
//			};
//			updateProfile(profile, profileUpdatedCallback);
//		}
	}

	public void updateProfile(final UserProfile newProfile, final AsyncCallback<Void> callback) {
		try { 
			JSONObjectAdapter adapter = newProfile.writeToJSONObject(jsonObjectAdapter.createNew());
			String userProfileJson = adapter.toJSONString();
	
			synapseClient.updateUserProfile(userProfileJson, new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					profile = newProfile;
					authenticationController.updateCachedProfile(profile);
					callback.onSuccess(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
		}
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
	
	@Override
	public void goToLastPlace() {
		view.hideLoggingInLoader();
		Place forwardPlace = globalApplicationState.getLastPlace();
		if(forwardPlace == null) {
			forwardPlace = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
		}
		bus.fireEvent(new PlaceChangeEvent(forwardPlace));
	}
	
	/*
	 * Private Methods
	 */
	
	
	public void showTermsOfUse(final AcceptTermsOfUseCallback callback) {
		authenticationController.getTermsOfUse(new AsyncCallback<String>() {
			public void onSuccess(String termsOfUseContents) {
				view.hideLoggingInLoader();
				view.showTermsOfUse(termsOfUseContents, callback);		
			}
			public void onFailure(Throwable t) {
				if(!DisplayUtils.checkForRepoDown(t, globalApplicationState.getPlaceChanger(), view)) 
					view.showErrorMessage("An error occurred. Please try logging in again.");
				view.showLogin(openIdActionUrl, openIdReturnUrl);									
			}
		});
	}
	
	private void revalidateSession(String token) {
		// Single Sign on token. try refreshing the token to see if it is valid. if so, log user in
		// parse token
		view.showLoggingInLoader();
		if(token != null) {
			final String sessionToken = token;
			
			AsyncCallback<String> callback = new AsyncCallback<String>() {	
				@Override
				public void onSuccess(String result) {
					if (!authenticationController.getCurrentUserSessionData().getSession().getAcceptsTermsOfUse()) {
						showTermsOfUse(new AcceptTermsOfUseCallback() {
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
											authenticationController.revalidateSession(sessionToken, new AsyncCallback<String>() {

												@Override
												public void onFailure(
														Throwable caught) {
													view.showErrorMessage("An error occurred. Please try logging in again.");
													view.showLogin(openIdActionUrl, openIdReturnUrl);
												}

												@Override
												public void onSuccess(
														String result) {
													// Signed ToU. Check for temp username, passing record, and then forward
													postLoginStep1();
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
											goToLastPlace();
										}
										
									});
								}
							});		
					} else {
						// user is logged in. forward to destination after checking for username
						postLoginStep1();
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
			};
			
			authenticationController.revalidateSession(sessionToken, callback);
		}
	}
	
	/**
	 * setter for user profile, for testing purposes only
	 * @param profile
	 */
	public void setUserProfile(UserProfile profile) {
		this.profile = profile;
	}

}
