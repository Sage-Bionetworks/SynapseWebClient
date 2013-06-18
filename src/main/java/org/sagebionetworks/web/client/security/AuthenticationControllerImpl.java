package org.sagebionetworks.web.client.security;

import java.util.Date;

import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

/**
 * A util class for authentication
 * 
 * **** DO NOT ADD NODE MODEL CREATOR OR ANY REPO MODEL OBJECTS TO THIS CLASS ****
 * (for code splitting purposes)
 * 
 * @author dburdick
 *
 */
public class AuthenticationControllerImpl implements AuthenticationController {
	
	private static final String SESSION_TOKEN = "sessionToken";
	private static final String PROFILE_KEY = "profile";
	private static final String OWNERID_KEY = "ownerId";
	private static final String isSSO_KEY = "isSSO";
	private static final String AUTHENTICATION_MESSAGE = "Invalid usename or password.";
	private static JSONObjectAdapter currentUser;
	
	private CookieProvider cookies;
	private UserAccountServiceAsync userAccountService;	
	private AdapterFactory adapterFactory;
	
	@Inject
	public AuthenticationControllerImpl(CookieProvider cookies, UserAccountServiceAsync userAccountService, AdapterFactory adapterFactory){
		this.cookies = cookies;
		this.userAccountService = userAccountService;
		this.adapterFactory = adapterFactory;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void loginUser(final String username, String password, boolean explicitlyAcceptsTermsOfUse, final AsyncCallback<String> callback) {
		if(username == null || password == null) callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));		
		userAccountService.initiateSession(username, password, explicitlyAcceptsTermsOfUse, new AsyncCallback<String>() {		
			@Override
			public void onSuccess(String userSessionJson) {
				JSONObjectAdapter userSessionData = null;
				try {
					//automatically expire after a day
					Date tomorrow = getDayFromNow();
					setUserSessionDataCookie(userSessionJson, tomorrow);
					userSessionData = adapterFactory.createNew(userSessionJson); 
					String sessionToken = getSessionToken(userSessionData);
					cookies.setCookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken, tomorrow);
				} catch (JSONObjectAdapterException e) {
					//can't save the cookie
					e.printStackTrace();
				}
				
				currentUser = userSessionData;
				callback.onSuccess(userSessionJson);
			}

			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	private Date getDayFromNow() {
		Date date = new Date();
		CalendarUtil.addDaysToDate(date, 1);
		return date;  
	}
	
	@Override
	public void loginUser(final String token, final AsyncCallback<String> callback) {
		setUser(token, callback, false);
	}
	
	@Override
	public void loginUserSSO(final String token, final AsyncCallback<String> callback) {
		setUser(token, callback, true);
	}

	@Override
	public void logoutUser() {
		String loginCookieString = cookies.getCookie(CookieKeys.USER_LOGIN_DATA);
		if(loginCookieString != null) {
			// don't actually terminate session, just remove the cookies			
			cookies.removeCookie(CookieKeys.USER_LOGIN_DATA);
			cookies.removeCookie(CookieKeys.USER_LOGIN_TOKEN);
			currentUser = null;
		}
	}	

	/*
	 * Private Methods
	 */
	@SuppressWarnings("deprecation")
	private void setUser(String token, final AsyncCallback<String> callback, final boolean isSSO) {
		if(token == null) callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
		userAccountService.getUser(token, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String userSessionJson) {
				if (userSessionJson != null) {					
					JSONObjectAdapter userSessionData = null;
					try {
						userSessionData = adapterFactory.createNew(userSessionJson);
						userSessionData.put(isSSO_KEY, isSSO);
						Date tomorrow = getDayFromNow();
						cookies.setCookie(CookieKeys.USER_LOGIN_DATA, userSessionData.toJSONString(), tomorrow);
						cookies.setCookie(CookieKeys.USER_LOGIN_TOKEN, getSessionToken(userSessionData), tomorrow);
					} catch (JSONObjectAdapterException e){
						callback.onFailure(e);
					}
					AuthenticationControllerImpl.currentUser = userSessionData;
					callback.onSuccess(userSessionData.toJSONString());
				} else {
					callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
			}
		});		
	}

	@Override
	public void getTermsOfUse(AsyncCallback<String> callback) {
		userAccountService.getTermsOfUse(callback);
	}

	@Override
	public boolean isLoggedIn() {
		String loginCookieString = cookies.getCookie(CookieKeys.USER_LOGIN_DATA);
		if(loginCookieString != null) {
			// remo.model.UserSessionData object, without using it directly
			try {
				JSONObjectAdapter userSessionDataObject = adapterFactory.createNew(loginCookieString);
				if(userSessionDataObject.has(SESSION_TOKEN)) {
					String token = userSessionDataObject.getString(SESSION_TOKEN);
					if(token != null && !token.isEmpty())
						return true;
				}
			} catch (JSONObjectAdapterException e) {
			}			
		} 
		return false;
	}

	@Override
	public String getCurrentUserPrincipalId() {
		String loginCookieString = cookies.getCookie(CookieKeys.USER_LOGIN_DATA);
		if(loginCookieString != null) {
			// remo.model.UserSessionData object, without using it directly
			try {
				JSONObjectAdapter userSessionDataObject = adapterFactory.createNew(loginCookieString);
				if(userSessionDataObject.has(PROFILE_KEY)) {
					JSONObjectAdapter profileObj = userSessionDataObject.getJSONObject(PROFILE_KEY);
					if(profileObj != null && profileObj.has(OWNERID_KEY)) {							
						return profileObj.getString(OWNERID_KEY);						
					}
				}
			} catch (JSONObjectAdapterException e) {
			}			
		} 
		return null;
	}

	
	@Override
	public void reloadUserSessionData() {
		String sessionToken = cookies.getCookie(CookieKeys.USER_LOGIN_TOKEN);
		setUser(sessionToken, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {				
			}

			@Override
			public void onSuccess(String result) {
			}
		}, getCurrentUserIsSSO());
		
	}

	@Override
	public JSONObjectAdapter getCurrentUserSessionData() {
		return currentUser;
	}

	@Override
	public String getCurrentUserSessionToken() {
		try {
			return getSessionToken(currentUser);
		} catch (JSONObjectAdapterException e) {
		}
		return null;
	}
	
	@Override
	public boolean getCurrentUserIsSSO() {
		if(currentUser != null) {
			try {
				return currentUser.getBoolean(isSSO_KEY);
			} catch (JSONObjectAdapterException e) {
			}
		}
		return false;
	}

	
	/*
	 * Private Methods
	 */
	private void setUserSessionDataCookie(String userSessionJson,
			Date tomorrow) {
		cookies.setCookie(CookieKeys.USER_LOGIN_DATA, userSessionJson, tomorrow);
	}

	private String getSessionToken(JSONObjectAdapter userSessionData)
			throws JSONObjectAdapterException {
		String sessionToken = null;
		if(userSessionData.has(SESSION_TOKEN))
			sessionToken = userSessionData.getString(SESSION_TOKEN);
		return sessionToken;
	}
	
}
