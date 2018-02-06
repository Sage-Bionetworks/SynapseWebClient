package org.sagebionetworks.web.client.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DateTimeUtilsImpl;
import org.sagebionetworks.web.client.GlobalApplicationStateImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
	private SynapseClientAsync synapseClient;
	private PortalGinInjector ginInjector;
	
	@Inject
	public AuthenticationControllerImpl(
			CookieProvider cookies, 
			UserAccountServiceAsync userAccountService, 
			SessionStorage sessionStorage, 
			ClientCache localStorage, 
			AdapterFactory adapterFactory,
			SynapseClientAsync synapseClient,
			PortalGinInjector ginInjector){
		this.cookies = cookies;
		this.userAccountService = userAccountService;
		this.sessionStorage = sessionStorage;
		this.localStorage = localStorage;
		this.adapterFactory = adapterFactory;
		this.synapseClient = synapseClient;
		this.ginInjector = ginInjector;
	}

	@Override
	public void loginUser(final String username, String password, final AsyncCallback<UserSessionData> callback) {
		if(username == null || password == null) callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
		LoginRequest loginRequest = getLoginRequest(username, password);
		ginInjector.getSynapseJavascriptClient().login(loginRequest, new AsyncCallback<LoginResponse>() {		
			@Override
			public void onSuccess(LoginResponse session) {
				storeAuthenticationReceipt(username, session.getAuthenticationReceipt());
				Session newSession = new Session();
				newSession.setAcceptsTermsOfUse(session.getAcceptsTermsOfUse());
				newSession.setSessionToken(session.getSessionToken());
				setSession(newSession, callback);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void storeAuthenticationReceipt(String username, String receipt) {
		localStorage.put(username + USER_AUTHENTICATION_RECEIPT, receipt, DateTimeUtilsImpl.getYearFromNow().getTime());
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
		localStorage.clear();
		initSynapsePropertiesFromServer();
		sessionStorage.clear();
		currentUser = null;
		ginInjector.getSessionTokenDetector().initializeSessionTokenState();
	}

	public void initSynapsePropertiesFromServer() {
		synapseClient.getSynapseProperties(new AsyncCallback<HashMap<String, String>>() {			
			@Override
			public void onSuccess(HashMap<String, String> properties) {
				for (String key : properties.keySet()) {
					localStorage.put(key, properties.get(key), DateTimeUtilsImpl.getYearFromNow().getTime());
				}
				localStorage.put(GlobalApplicationStateImpl.PROPERTIES_LOADED_KEY, Boolean.TRUE.toString(), DateTimeUtilsImpl.getWeekFromNow().getTime());
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	private void setUser(final String token, final AsyncCallback<UserSessionData> callback) {
		if(token == null) {
			sessionStorage.clear();
			callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
			return;
		}
		
		ginInjector.getSynapseJavascriptClient().revalidate(token, new AsyncCallback<Session>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof SynapseDownException || caught instanceof ReadOnlyModeException) {
					ginInjector.getGlobalApplicationState().getPlaceChanger().goTo(new Down(ClientProperties.DEFAULT_PLACE_TOKEN));
				} else {
					logoutUser();
					callback.onFailure(caught);
				}
			}
			@Override
			public void onSuccess(final Session session) {
				// session token is valid
				Date tomorrow = DateTimeUtilsImpl.getDayFromNow();
				cookies.setCookie(CookieKeys.USER_LOGGED_IN_RECENTLY, "true", DateTimeUtilsImpl.getWeekFromNow());
				cookies.setCookie(CookieKeys.USER_LOGIN_TOKEN, token, tomorrow);
				if (currentUser == null) {
					setSession(session, callback);
				} else {
					setUserSessionData(currentUser, callback);
				}
			}
		});
	}
	
	private void setSession(Session session, final AsyncCallback<UserSessionData> callback) {
		// get user profile and then set user session data
		ginInjector.getSynapseJavascriptClient().getMyUserProfile(new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile userProfile) {
				// session token is valid, and now we have a user profile
				UserSessionData usd = new UserSessionData();
				usd.setProfile(userProfile);
				usd.setSession(session);
				setUserSessionData(usd, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	private void setUserSessionData(UserSessionData userSessionData, AsyncCallback<UserSessionData> callback) {
		Date tomorrow = DateTimeUtilsImpl.getDayFromNow();
		currentUser = userSessionData;
		localStorage.put(USER_SESSION_DATA_CACHE_KEY, getUserSessionDataString(currentUser), tomorrow.getTime());
		ginInjector.getSessionTokenDetector().initializeSessionTokenState();
		callback.onSuccess(currentUser);
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
			Date tomorrow = DateTimeUtilsImpl.getDayFromNow();
			localStorage.put(USER_SESSION_DATA_CACHE_KEY, getUserSessionDataString(currentUser), tomorrow.getTime());
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
	
	@Override
	public void checkForUserChange() {
		String currentSession = cookies.getCookie(CookieKeys.USER_LOGIN_TOKEN);
		String localSession = getCurrentUserSessionToken();
		if (!Objects.equals(currentSession, localSession)) {
			Window.Location.reload();
		}
		
		//also revalidate user session
		if (currentSession != null) {
			revalidateSession(currentSession, new AsyncCallback<UserSessionData>() {
				@Override
				public void onSuccess(UserSessionData result) {
					// still valid (and call has updated expiration)
				}
				
				@Override
				public void onFailure(Throwable caught) {
					//invalid session token
					Window.Location.reload();
				}
			});	
		}
	}
}
