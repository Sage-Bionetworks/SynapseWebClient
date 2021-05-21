package org.sagebionetworks.web.client.security;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.Objects;

import org.sagebionetworks.repo.model.UserProfile;
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
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
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
	private static String currentUserAccessToken;
	private static UserProfile currentUserProfile;
	private UserAccountServiceAsync userAccountService;
	private ClientCache localStorage;
	private SessionStorage sessionStorage;
	private PortalGinInjector ginInjector;
	private SynapseJSNIUtils jsniUtils;
	private CookieProvider cookies;

	@Inject
	public AuthenticationControllerImpl(UserAccountServiceAsync userAccountService, ClientCache localStorage, SessionStorage sessionStorage, CookieProvider cookies, PortalGinInjector ginInjector, SynapseJSNIUtils jsniUtils) {
		this.userAccountService = userAccountService;
		fixServiceEntryPoint(userAccountService);
		this.localStorage = localStorage;
		this.sessionStorage = sessionStorage;
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
			public void onSuccess(LoginResponse response) {
				storeAuthenticationReceipt(response.getAuthenticationReceipt());
				setNewAccessToken(response.getAccessToken(), callback);
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
	 * Called to update the access token.
	 * 
	 * @param token
	 * @param callback
	 */
	public void setNewAccessToken(String token, AsyncCallback<UserProfile> callback) {
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
				initializeFromExistingAccessTokenCookie(callback);
			}
		});
	}

	/**
	 * Access token cookie should be set before this call
	 * 
	 * @param callback
	 */
	public void initializeFromExistingAccessTokenCookie(AsyncCallback<UserProfile> callback) {
		// attempt to detect the current access token.  if found, get the associated user profile.  if forbidden (due to ToU), send to ToU page.
		FluentFuture<String> accessTokenFuture = ginInjector.getSynapseJavascriptClient().getAccessToken();
		accessTokenFuture.addCallback(new FutureCallback<String>() {
			@Override
			public void onSuccess(String accessToken) {
				cookies.setCookie(CookieKeys.USER_LOGGED_IN_RECENTLY, "true", DateTimeUtilsImpl.getWeekFromNow());
				currentUserAccessToken = accessToken;
				FluentFuture<UserProfile> userProfileFuture = ginInjector.getSynapseJavascriptClient().getMyUserProfile();
				userProfileFuture.addCallback(new FutureCallback<UserProfile>() {
					@Override
					public void onSuccess(UserProfile profile) {
						currentUserProfile = profile;
						ginInjector.getSessionDetector().initializeAccessTokenState();
						jsniUtils.setAnalyticsUserId(getCurrentUserPrincipalId());
						callback.onSuccess(currentUserProfile);
					}
					@Override
					public void onFailure(Throwable t) {
						currentUserProfile = null;
						ginInjector.getSessionDetector().initializeAccessTokenState();
						if (t instanceof ForbiddenException && ((ForbiddenException)t).getMessage().toLowerCase().contains("terms of use")) {
							ginInjector.getGlobalApplicationState().getPlaceChanger().goTo(new LoginPlace(LoginPlace.SHOW_TOU));
						} else {
							callback.onFailure(t);
						}
					}
				}, directExecutor());
			}

			@Override
			public void onFailure(Throwable t) {
				currentUserAccessToken = null;
				currentUserProfile = null;
				ginInjector.getSessionDetector().initializeAccessTokenState();
				callback.onFailure(t);
			}
		}, directExecutor());
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
		jsniUtils.setAnalyticsUserId("");
		String receipt = localStorage.get(USER_AUTHENTICATION_RECEIPT);
		localStorage.clear();
		storeAuthenticationReceipt(receipt);
		// save last place but clear other session storage values on logout.
		Place lastPlace = ginInjector.getGlobalApplicationState().getLastPlace();
		sessionStorage.clear();
		ginInjector.getGlobalApplicationState().setLastPlace(lastPlace);
		currentUserAccessToken = null;
		currentUserProfile = null;
		ginInjector.getSessionDetector().initializeAccessTokenState();
		ginInjector.getSynapseJavascriptClient().initSession(WebConstants.EXPIRE_SESSION_TOKEN);
		ginInjector.getGlobalApplicationState().refreshPage();
	}

	@Override
	public void updateCachedProfile(UserProfile updatedProfile) {
		currentUserProfile = updatedProfile;
	}

	@Override
	public boolean isLoggedIn() {
		return currentUserAccessToken != null && !currentUserAccessToken.isEmpty() && currentUserProfile != null;
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
	public String getCurrentUserAccessToken() {
		return currentUserAccessToken;
	}

	@Override
	public void signTermsOfUse(AsyncCallback<Void> callback) {
		userAccountService.signTermsOfUse(getCurrentUserAccessToken(), callback);
	}

	@Override
	public void checkForUserChange() {
		checkForUserChange(null);
	}
	
	@Override
	public void checkForUserChange(Callback webAppInitializationCallback) {
		String oldUserAccessToken = currentUserAccessToken;
		initializeFromExistingAccessTokenCookie(new AsyncCallback<UserProfile>() {
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught);
				if (webAppInitializationCallback != null) {
					webAppInitializationCallback.invoke();
				} else {
					// if the exception was not due to a network failure, then log the user out
					if (!(caught instanceof UnknownErrorException || caught instanceof StatusCodeException)) {
						logoutUser();	
					}
				}
			}

			@Override
			public void onSuccess(UserProfile result) {
				// is this a user session change?  if so, refresh the page.
				if (!Objects.equals(currentUserAccessToken, oldUserAccessToken)) {
					// we've reinitialized the app with the correct session, refresh the page (do not get rid of js state)!
					if (webAppInitializationCallback != null) {
						webAppInitializationCallback.invoke();
					} else {
						ginInjector.getGlobalApplicationState().refreshPage();	
					}
					checkForQuarantinedEmail();
				} else {
					ginInjector.getHeader().refresh();
					// we've determined that the session has not changed
					if (webAppInitializationCallback != null) {
						webAppInitializationCallback.invoke();
					} 
				}
			}
		});
	}
}
