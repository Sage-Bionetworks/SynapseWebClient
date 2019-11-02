package org.sagebionetworks.web.client.security;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.Objects;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.principal.EmailQuarantineReason;
import org.sagebionetworks.repo.model.principal.EmailQuarantineStatus;
import org.sagebionetworks.repo.model.principal.NotificationEmail;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DateTimeUtilsImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
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
	public static final String USER_AUTHENTICATION_RECEIPT = "last_user_authentication_receipt";
	private static final String AUTHENTICATION_MESSAGE = "Invalid username or password.";
	private static String currentUserSessionToken;
	private static UserProfile currentUserProfile;
	private UserAccountServiceAsync userAccountService;
	private ClientCache localStorage;
	private PortalGinInjector ginInjector;
	private SynapseJSNIUtils jsniUtils;
	private CookieProvider cookies;

	@Inject
	public AuthenticationControllerImpl(UserAccountServiceAsync userAccountService, ClientCache localStorage, CookieProvider cookies, PortalGinInjector ginInjector, SynapseJSNIUtils jsniUtils) {
		this.userAccountService = userAccountService;
		fixServiceEntryPoint(userAccountService);
		this.localStorage = localStorage;
		this.cookies = cookies;
		this.ginInjector = ginInjector;
		this.jsniUtils = jsniUtils;
	}

	@Override
	public void loginUser(final String username, String password, final AsyncCallback<UserProfile> callback) {
		if (username == null || password == null)
			callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
		LoginRequest loginRequest = getLoginRequest(username, password);
		ginInjector.getSynapseJavascriptClient().login(loginRequest, new AsyncCallback<LoginResponse>() {
			@Override
			public void onSuccess(LoginResponse session) {
				storeAuthenticationReceipt(session.getAuthenticationReceipt());
				setNewSessionToken(session.getSessionToken(), callback);
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	public void storeAuthenticationReceipt(String receipt) {
		localStorage.put(USER_AUTHENTICATION_RECEIPT, receipt, DateTimeUtilsImpl.getYearFromNow().getTime());
	}

	public LoginRequest getLoginRequest(String username, String password) {
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);
		String authenticationReceipt = localStorage.get(USER_AUTHENTICATION_RECEIPT);
		request.setAuthenticationReceipt(authenticationReceipt);
		return request;
	}

	/**
	 * Called to update the session token.
	 * 
	 * @param token
	 * @param callback
	 */
	public void setNewSessionToken(String token, AsyncCallback<UserProfile> callback) {
		if (token == null) {
			callback.onFailure(new AuthenticationException(AUTHENTICATION_MESSAGE));
			return;
		}
		ginInjector.getSynapseJavascriptClient().initSession(token, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				logoutUser();
				if (caught instanceof SynapseDownException || caught instanceof ReadOnlyModeException) {
					ginInjector.getGlobalApplicationState().getPlaceChanger().goTo(new Down(ClientProperties.DEFAULT_PLACE_TOKEN));
				} else {
					callback.onFailure(caught);
				}
			}

			@Override
			public void onSuccess(Void result) {
				initializeFromExistingSessionCookie(callback);
			}
		});
	}

	/**
	 * Session cookie should be set before this call
	 * 
	 * @param callback
	 */
	public void initializeFromExistingSessionCookie(AsyncCallback<UserProfile> callback) {
		userAccountService.getCurrentUserSessionData(new AsyncCallback<UserSessionData>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(UserSessionData newData) {
				cookies.setCookie(CookieKeys.USER_LOGGED_IN_RECENTLY, "true", DateTimeUtilsImpl.getWeekFromNow());
				currentUserSessionToken = newData.getSession().getSessionToken();
				currentUserProfile = newData.getProfile();
				ginInjector.getSessionDetector().initializeSessionTokenState();
				if (!newData.getSession().getAcceptsTermsOfUse()) {
					ginInjector.getGlobalApplicationState().getPlaceChanger().goTo(new LoginPlace(LoginPlace.SHOW_TOU));
				} else {
					jsniUtils.setAnalyticsUserId(getCurrentUserPrincipalId());
					callback.onSuccess(newData.getProfile());
				}
			}
		});
	}

	public void checkForQuarantinedEmail() {
		ginInjector.getSynapseJavascriptClient().getNotificationEmail(new AsyncCallback<NotificationEmail>() {
			@Override
			public void onSuccess(NotificationEmail notificationEmailStatus) {
				EmailQuarantineStatus status = notificationEmailStatus.getQuarantineStatus();
				if (isQuarantined(status)) {
					ginInjector.getQuarantinedEmailModal().show(status.getReasonDetails());
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught);
			}
		});
	}

	public static boolean isQuarantined(EmailQuarantineStatus status) {
		return status != null && EmailQuarantineReason.PERMANENT_BOUNCE.equals(status.getReason());
	}

	@Override
	public void logoutUser() {
		// terminate the session, remove the cookie
		ginInjector.getSynapseJavascriptClient().logout();
		jsniUtils.setAnalyticsUserId("");
		String receipt = localStorage.get(USER_AUTHENTICATION_RECEIPT);
		localStorage.clear();
		storeAuthenticationReceipt(receipt);
		currentUserSessionToken = null;
		currentUserProfile = null;
		ginInjector.getSessionDetector().initializeSessionTokenState();
		ginInjector.getSynapseJavascriptClient().initSession(WebConstants.EXPIRE_SESSION_TOKEN);
		ginInjector.getGlobalApplicationState().refreshPage();
	}

	@Override
	public void updateCachedProfile(UserProfile updatedProfile) {
		currentUserProfile = updatedProfile;
	}

	@Override
	public boolean isLoggedIn() {
		return currentUserSessionToken != null && !currentUserSessionToken.isEmpty() && currentUserProfile != null;
	}

	@Override
	public String getCurrentUserPrincipalId() {
		if (currentUserProfile != null) {
			return currentUserProfile.getOwnerId();
		}
		return null;
	}

	@Override
	public UserProfile getCurrentUserProfile() {
		return currentUserProfile;
	}

	@Override
	public String getCurrentUserSessionToken() {
		return currentUserSessionToken;
	}

	@Override
	public void signTermsOfUse(boolean accepted, AsyncCallback<Void> callback) {
		userAccountService.signTermsOfUse(getCurrentUserSessionToken(), accepted, callback);
	}

	@Override
	public void checkForUserChange() {
		userAccountService.getCurrentSessionToken(new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught);
				logoutUser();
			}

			@Override
			public void onSuccess(String token) {
				String localSession = getCurrentUserSessionToken();
				// if the local session does not match the actual session, reload the app
				if (!Objects.equals(token, localSession)) {
					// reinit
					if (token == null) {
						logoutUser();
					} else {
						initializeFromExistingSessionCookie(new AsyncCallback<UserProfile>() {
							@Override
							public void onFailure(Throwable caught) {
								jsniUtils.consoleError(caught);
							}

							@Override
							public void onSuccess(UserProfile result) {
								// we've reinitialized the app with the correct session, refresh the page (do not get rid of js
								// state)!
								ginInjector.getGlobalApplicationState().refreshPage();
								checkForQuarantinedEmail();
							}
						});
					}
				} else {
					ginInjector.getHeader().refresh();
					if (isLoggedIn()) {
						// we've determined that the session has not changed, update the cookie expiration for the session
						// token
						setNewSessionToken(currentUserSessionToken, new AsyncCallback<UserProfile>() {
							@Override
							public void onFailure(Throwable caught) {
								jsniUtils.consoleError(caught);
							}

							@Override
							public void onSuccess(UserProfile result) {
								// the set-cookie response header has updated the expiration of the session token cookie
							}
						});
					}
				}
			}
		});
	}

	@Override
	public void refreshSessionToken() {
		ginInjector.getSynapseJavascriptClient().refreshCurrentSessionToken();
	}
}
