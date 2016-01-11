package org.sagebionetworks.web.client.security;

import java.util.Date;

import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.shared.UserLoginBundle;

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
	private static UserSessionData currentUser;
	private static UserBundle userBundle;
	
	private CookieProvider cookies;
	private UserAccountServiceAsync userAccountService;	
	
	@Inject
	public AuthenticationControllerImpl(CookieProvider cookies, UserAccountServiceAsync userAccountService) {
		this.cookies = cookies;
		this.userAccountService = userAccountService;
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
		if(token == null) {
			callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
			return;
		}
		// clear out old userBundle
		userBundle = null;
		userAccountService.getUserLoginBundle(token, new AsyncCallback<UserLoginBundle>() {
			@Override
			public void onSuccess(UserLoginBundle userLoginBundle) {
				UserSessionData userSessionData = userLoginBundle.getUserSessionData();
				UserBundle fetchedUserBundle = userLoginBundle.getUserBundle();
				if (userSessionData != null && fetchedUserBundle != null) {					
					Date tomorrow = getDayFromNow();
					cookies.setCookie(CookieKeys.USER_LOGGED_IN_RECENTLY, "true", getWeekFromNow());
					cookies.setCookie(CookieKeys.USER_LOGIN_TOKEN, userSessionData.getSession().getSessionToken(), tomorrow);
					currentUser = userSessionData;
					userBundle = fetchedUserBundle;
					callback.onSuccess(userSessionData);
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
	public void logoutUser() {
		// don't actually terminate session, just remove the cookie
		cookies.removeCookie(CookieKeys.USER_LOGIN_TOKEN);
		currentUser = null;
		userBundle = null;
	}

	@Override
	public void updateCachedProfile(UserProfile updatedProfile){
		if (isLoggedIn()) {
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
		boolean isValidToken = token != null && !token.isEmpty();
		// Checks currentuser and userBundle for existence and matching
		boolean isValidUserState = currentUser != null && userBundle != null && currentUser.getProfile() != null
				&& userBundle.getUserId().equals(currentUser.getProfile().getOwnerId());
		return  isValidToken && isValidUserState;
	}

	@Override
	public String getCurrentUserPrincipalId() {
		if (isLoggedIn()) {		
			return currentUser.getProfile().getOwnerId();						
		} 
		return null;
	}
	
	@Override
	public void reloadUserSessionData(AsyncCallback<UserSessionData> callback) {
		String sessionToken = cookies.getCookie(CookieKeys.USER_LOGIN_TOKEN);
		revalidateSession(sessionToken, callback);
	}

	@Override
	public UserSessionData getCurrentUserSessionData() {
		if (isLoggedIn()) {
			return currentUser;
		} else {
			return null;
		}
	}

	@Override
	public String getCurrentUserSessionToken() {
		if (isLoggedIn()) {
			return currentUser.getSession().getSessionToken();
		} else {
			return null;
		}
	}
	
	@Override
	public UserBundle getCurrentUserBundle() {
		if (isLoggedIn()) {
			return userBundle;
		} else {
			return null;
		}
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
