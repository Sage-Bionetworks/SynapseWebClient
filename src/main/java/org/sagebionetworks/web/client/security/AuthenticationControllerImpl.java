package org.sagebionetworks.web.client.security;

import java.util.Date;

import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

/**
 * A util class for authentication
 * 
 * CODE SPLITTING NOTE: this class should be kept small
 * 
 * @author dburdick
 *
 */
public class AuthenticationControllerImpl implements AuthenticationController {
	private static final String AUTHENTICATION_MESSAGE = "Invalid usename or password.";
	private static final String USER_BUNDLE_FETCH_ERROR_MESSAGE = "Failed to fetch User Bundle.";
	private static UserSessionData currentUser;
	private static UserBundle userBundle;
	
	private CookieProvider cookies;
	private UserAccountServiceAsync userAccountService;	
	private AdapterFactory adapterFactory;
	private UserProfileClientAsync userProfileClient;
	
	@Inject
	public AuthenticationControllerImpl(CookieProvider cookies, UserAccountServiceAsync userAccountService, AdapterFactory adapterFactory,
			UserProfileClientAsync userProfileClient){
		this.cookies = cookies;
		this.userAccountService = userAccountService;
		this.adapterFactory = adapterFactory;
		this.userProfileClient = userProfileClient;
	}

	@Override
	public void loginUser(final String username, String password, final AsyncCallback<UserSessionData> callback) {
		if(username == null || password == null) callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));		
		userAccountService.initiateSession(username, password, new AsyncCallback<Session>() {		
			@Override
			public void onSuccess(Session session) {				
				revalidateSession(session.getSessionToken(), callback);
			}
			
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public void revalidateSession(final String token, final AsyncCallback<UserSessionData> callback) {
		setUser(token, callback);
	}

	@Override
	public void logoutUser() {
		// don't actually terminate session, just remove the cookie
		cookies.removeCookie(CookieKeys.USER_LOGIN_TOKEN);
		currentUser = null;
	}

	private void setUser(String token, final AsyncCallback<UserSessionData> callback) {
		if(token == null) {
			callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
			return;
		}
		userAccountService.getUserSessionData(token, new AsyncCallback<UserSessionData>() {
			@Override
			public void onSuccess(UserSessionData userSessionData) {
				if (userSessionData != null) {					
					Date tomorrow = getDayFromNow();
					cookies.setCookie(CookieKeys.USER_LOGGED_IN_RECENTLY, "true", getWeekFromNow());
					cookies.setCookie(CookieKeys.USER_LOGIN_TOKEN, userSessionData.getSession().getSessionToken(), tomorrow);
					currentUser = userSessionData;
					callback.onSuccess(userSessionData);
					
					// Attempt to get UserBundle information for user being set, first resetting userBundle
					userBundle = null;
					// 63 is the mask equivalent for getting every UserBundle component
					userProfileClient.getUserBundle(Long.valueOf(userSessionData.getProfile().getOwnerId()), 63, new AsyncCallback<UserBundle>() {
						@Override
						public void onFailure(Throwable e) {
							// log in JS console?
						}
						@Override
						public void onSuccess(UserBundle bundle) {
							userBundle = bundle;
						}
					});
				} else {
					onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				logoutUser();
				callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE + " " + caught.getMessage()));
			}
		});		
	}

	@Override
	public void updateCachedProfile(UserProfile updatedProfile){
		if(currentUser != null) {
			currentUser.setProfile(updatedProfile);
		}
	}
	
	@Override
	public void getTermsOfUse(AsyncCallback<String> callback) {
		userAccountService.getTermsOfUse(callback);
	}

	@Override
	public boolean isLoggedIn() {
		String token = cookies.getCookie(CookieKeys.USER_LOGIN_TOKEN);
		return token != null && !token.isEmpty() && currentUser != null;
	}

	@Override
	public String getCurrentUserPrincipalId() {
		if(currentUser != null) {		
			UserProfile profileObj = currentUser.getProfile();
			if(profileObj != null && profileObj.getOwnerId() != null) {							
				return profileObj.getOwnerId();						
			}
		} 
		return null;
	}
	
	@Override
	public void reloadUserSessionData(AsyncCallback<UserSessionData> callback) {
		String sessionToken = cookies.getCookie(CookieKeys.USER_LOGIN_TOKEN);
		setUser(sessionToken, callback);
	}

	@Override
	public UserSessionData getCurrentUserSessionData() {
		if (isLoggedIn()) {
			return currentUser;
		} else
			return null;
	}

	@Override
	public String getCurrentUserSessionToken() {
		if(currentUser != null) return currentUser.getSession().getSessionToken();
		else return null;
	}
	
	@Override
	public void signTermsOfUse(boolean accepted, AsyncCallback<Void> callback) {
		userAccountService.signTermsOfUse(getCurrentUserSessionToken(), accepted, callback);
	}

	private Date getDayFromNow() {
		Date date = new Date();
		CalendarUtil.addDaysToDate(date, 1);
		return date;  
	}
	
	private Date getWeekFromNow() {
		Date date = new Date();
		CalendarUtil.addDaysToDate(date, 7);
		return date;  
	}
}
