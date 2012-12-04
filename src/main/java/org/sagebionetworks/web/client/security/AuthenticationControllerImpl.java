package org.sagebionetworks.web.client.security;

import java.util.Date;

import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

public class AuthenticationControllerImpl implements AuthenticationController {
	
	private static String AUTHENTICATION_MESSAGE = "Invalid usename or password.";
	private static UserSessionData currentUser;
	
	private CookieProvider cookies;
	private UserAccountServiceAsync userAccountService;
	private NodeModelCreator nodeModelCreator;

	@Inject
	public AuthenticationControllerImpl(CookieProvider cookies, UserAccountServiceAsync userAccountService, NodeModelCreator nodeModelCreator){
		this.cookies = cookies;
		this.userAccountService = userAccountService;
		this.nodeModelCreator = nodeModelCreator;
	}

	@Override
	public boolean isLoggedIn() {
		String loginCookieString = cookies.getCookie(CookieKeys.USER_LOGIN_DATA);
		if(loginCookieString != null) {
			try {
				currentUser = nodeModelCreator.createJSONEntity(loginCookieString, UserSessionData.class);
			} catch (Throwable e) {
				//invalid user
				e.printStackTrace();
			}
			if(currentUser != null)
				return true;
		} 
		return false;
	}

	@Override
	public void loginUser(final String username, String password, boolean explicitlyAcceptsTermsOfUse, final AsyncCallback<String> callback) {
		if(username == null || password == null) callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));		
		userAccountService.initiateSession(username, password, explicitlyAcceptsTermsOfUse, new AsyncCallback<String>() {		
			@Override
			public void onSuccess(String userSessionJson) {
				UserSessionData userSessionData = null;
				try {
					//automatically expire after a day
					Date tomorrow = getDayFromNow();
					cookies.setCookie(CookieKeys.USER_LOGIN_DATA, userSessionJson, tomorrow);
					userSessionData = nodeModelCreator.createJSONEntity(userSessionJson, UserSessionData.class);
					cookies.setCookie(CookieKeys.USER_LOGIN_TOKEN, userSessionData.getSessionToken(), tomorrow);
				} catch (JSONObjectAdapterException e) {
					//can't save the cookie
					e.printStackTrace();
				}
				
				AuthenticationControllerImpl.currentUser = userSessionData;
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
	public UserSessionData getLoggedInUser() {
		if (isLoggedIn()) {
			return currentUser;
		}
		else return null;
	}

	@Override
	public void logoutUser() {
		if(currentUser != null) {
			// don't actually terminate session, just remove the cookies			
			cookies.removeCookie(CookieKeys.USER_LOGIN_DATA);
			cookies.removeCookie(CookieKeys.USER_LOGIN_TOKEN);
			currentUser = null;
		}
	}	

	/*
	 * Private Methods
	 */
	private void setUser(String token, final AsyncCallback<String> callback, final boolean isSSO) {
		if(token == null) callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
		userAccountService.getUser(token, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String userSessionJson) {
				if (userSessionJson != null) {
					String updatedSessionJson = userSessionJson;
					UserSessionData userSessionData = null;
					try {
						userSessionData = nodeModelCreator.createJSONEntity(userSessionJson, UserSessionData.class);
						userSessionData.setIsSSO(isSSO);
						JSONObjectAdapter adapter = userSessionData.writeToJSONObject(JSONObjectGwt.createNewAdapter());
						updatedSessionJson = adapter.toJSONString();
						Date tomorrow = getDayFromNow();
						cookies.setCookie(CookieKeys.USER_LOGIN_DATA, updatedSessionJson, tomorrow);
						cookies.setCookie(CookieKeys.USER_LOGIN_TOKEN, userSessionData.getSessionToken(), tomorrow);
					} catch (JSONObjectAdapterException e){
						callback.onFailure(e);
					}
					AuthenticationControllerImpl.currentUser = userSessionData;
					callback.onSuccess(updatedSessionJson);
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

}
