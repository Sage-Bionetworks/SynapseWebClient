package org.sagebionetworks.web.client.security;

import java.util.Date;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.shared.WebConstants;

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
	
	private CookieProvider cookies;
	private UserAccountServiceAsync userAccountService;	
	private AdapterFactory adapterFactory;
	
	@Inject
	public AuthenticationControllerImpl(CookieProvider cookies, UserAccountServiceAsync userAccountService, AdapterFactory adapterFactory){
		this.cookies = cookies;
		this.userAccountService = userAccountService;
		this.adapterFactory = adapterFactory;
	}

	@Override
	public void loginUser(final String username, String password, final AsyncCallback<String> callback) {
		if(username == null || password == null) callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));		
		userAccountService.initiateSession(username, password, new AsyncCallback<String>() {		
			@Override
			public void onSuccess(String sessionJson) {
				Session session = null;
				try {
					session = new Session(adapterFactory.createNew(sessionJson));
				} catch (JSONObjectAdapterException e) {
					//can't save the cookie
					e.printStackTrace();
				}
				
				revalidateSession(session.getSessionToken(), callback);
			}

			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public void revalidateSession(final String token, final AsyncCallback<String> callback) {
		setUser(token, callback);
	}

	@Override
	public void logoutUser() {
		// don't actually terminate session, just remove the cookie
		cookies.removeCookie(CookieKeys.USER_LOGIN_TOKEN);
		currentUser = null;
	}

	private void setUser(String token, final AsyncCallback<String> callback) {
		if(token == null) callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
		userAccountService.getUserSessionData(token, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String userSessionJson) {
				if (userSessionJson != null) {					
					UserSessionData userSessionData = null;
					try {
						JSONObjectAdapter usdAdapter = adapterFactory.createNew(userSessionJson);
						userSessionData = new UserSessionData(usdAdapter);
						Date tomorrow = getDayFromNow();
						cookies.setCookie(CookieKeys.USER_LOGIN_TOKEN, userSessionData.getSession().getSessionToken(), tomorrow);
						currentUser = userSessionData;
						callback.onSuccess(usdAdapter.toJSONString());
					} catch (JSONObjectAdapterException e){
						callback.onFailure(e);
					}
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
	public void reloadUserSessionData(AsyncCallback<String> callback) {
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
}
