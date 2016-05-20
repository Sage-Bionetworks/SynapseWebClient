package org.sagebionetworks.web.client.security;

import java.util.Date;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.core.client.GWT;
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
	public static final String USER_SESSION_DATA_CACHE_KEY = "org.sagebionetworks.UserSessionData";
	public static final String USER_AUTHENTICATION_RECEIPT = "_authentication_receipt";
	private static final String AUTHENTICATION_MESSAGE = "Invalid usename or password.";
	private static UserSessionData currentUser;
	
	private CookieProvider cookies;
	private UserAccountServiceAsync userAccountService;	
	private SessionStorage sessionStorage;
	private ClientCache localStorage;
	private AdapterFactory adapterFactory;
	
	@Inject
	public AuthenticationControllerImpl(
			CookieProvider cookies, 
			UserAccountServiceAsync userAccountService, 
			SessionStorage sessionStorage, 
			ClientCache localStorage, 
			AdapterFactory adapterFactory){
		this.cookies = cookies;
		this.userAccountService = userAccountService;
		this.sessionStorage = sessionStorage;
		this.localStorage = localStorage;
		this.adapterFactory = adapterFactory;
	}

	@Override
	public void loginUser(final String username, String password, final AsyncCallback<UserSessionData> callback) {
		if(username == null || password == null) callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
		LoginRequest loginRequest = getLoginRequest(username, password);
		userAccountService.initiateSession(loginRequest, new AsyncCallback<LoginResponse>() {		
			@Override
			public void onSuccess(LoginResponse session) {
				storeAuthenticationReceipt(username, session.getAuthenticationReceipt());
				revalidateSession(session.getSessionToken(), callback);
			}
			
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void storeAuthenticationReceipt(String username, String receipt) {
		localStorage.put(username + USER_AUTHENTICATION_RECEIPT, receipt, getYearFromNow().getTime());
	}
	
	public LoginRequest getLoginRequest(String username, String password) {
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);
		String authenticationReceipt = localStorage.get(username + USER_AUTHENTICATION_RECEIPT);
		request.setAuthenticationReceipt(authenticationReceipt);
		return request;
	}
	
	@Override
	public void revalidateSession(final String token, final AsyncCallback<UserSessionData> callback) {
		setUser(token, callback);
	}

	@Override
	public void logoutUser() {
		// don't actually terminate session, just remove the cookie
		cookies.removeCookie(CookieKeys.USER_LOGIN_TOKEN);
		localStorage.remove(USER_SESSION_DATA_CACHE_KEY);
		sessionStorage.clear();
		currentUser = null;
	}

	private void setUser(String token, final AsyncCallback<UserSessionData> callback) {
		if(token == null) {
			sessionStorage.clear();
			callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
			return;
		}
		
		userAccountService.getUserSessionData(token, new AsyncCallback<UserSessionData>() {
			@Override
			public void onSuccess(UserSessionData userSessionData) {
				Date tomorrow = getDayFromNow();
				cookies.setCookie(CookieKeys.USER_LOGGED_IN_RECENTLY, "true", getWeekFromNow());
				cookies.setCookie(CookieKeys.USER_LOGIN_TOKEN, userSessionData.getSession().getSessionToken(), tomorrow);
				currentUser = userSessionData;
				localStorage.put(USER_SESSION_DATA_CACHE_KEY, getUserSessionDataString(currentUser), tomorrow.getTime());
				callback.onSuccess(userSessionData);
			}
			@Override
			public void onFailure(Throwable caught) {
				logoutUser();
				callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE + " " + caught.getMessage()));
			}
		});
	}

	public String getUserSessionDataString(UserSessionData session) {
		JSONObjectAdapter adapter = adapterFactory.createNew();
		try {
			session.writeToJSONObject(adapter);
			return adapter.toJSONString();
		} catch (JSONObjectAdapterException e) {
			return null;
		}
	}
	
	public UserSessionData getUserSessionData(String sessionString) {
		try {
			return new UserSessionData(adapterFactory.createNew(sessionString));
		} catch (JSONObjectAdapterException e) {
			return null;
		}
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
	public void reloadUserSessionData() {
		String sessionToken = cookies.getCookie(CookieKeys.USER_LOGIN_TOKEN);
		// try to set current user and bundle from session cache
		if (sessionToken != null) {
			// load user session data from session storage
			String sessionStorageString = localStorage.get(USER_SESSION_DATA_CACHE_KEY);
			if (sessionStorageString != null) {
				currentUser = getUserSessionData(sessionStorageString);
			} else {
				logoutUser();
			}
		}
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
	
	private Date getYearFromNow() {
		Date date = new Date();
		CalendarUtil.addMonthsToDate(date, 12);
		return date;  
	}
}
