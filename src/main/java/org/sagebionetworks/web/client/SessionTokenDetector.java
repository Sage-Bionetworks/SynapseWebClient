package org.sagebionetworks.web.client;

import static org.sagebionetworks.web.client.cookie.CookieKeys.USER_LOGIN_TOKEN;
import java.util.Objects;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.inject.Inject;

public class SessionTokenDetector {

	public static final int INTERVAL_MS = 1000*10; //check every 10 seconds
	CookieProvider cookies;
	AuthenticationController authController;
	GlobalApplicationState globalAppState;
	GWTWrapper gwt;
	boolean isSessionToken;
	@Inject
	public SessionTokenDetector(
			CookieProvider cookies, 
			AuthenticationController authController,
			GlobalApplicationState globalAppState,
			GWTWrapper gwt) {
		this.cookies = cookies;
		this.authController = authController;
		this.globalAppState = globalAppState;
		this.gwt = gwt;
		initializeSessionTokenState();
	}
	
	private boolean isSessionTokenCookie() {
		return cookies.getCookie(USER_LOGIN_TOKEN) != null;
	}
	
	public void start() {
		gwt.scheduleFixedDelay(() -> {
			boolean isNowSessionToken = isSessionTokenCookie();
			if (!Objects.equals(isSessionToken, isNowSessionToken)) {
				// session token state mismatch, update the app state (by loading the current session data, or clearing) and refresh the page (not an app reload).
				if (isNowSessionToken) {
					authController.reloadUserSessionData();
				} else {
					authController.logoutUser();
				}
				globalAppState.refreshPage();
			}
		}, INTERVAL_MS);
	}
	
	public void initializeSessionTokenState() {
		//if user explicitly logs out or in, then there's no need to reload when detecting state change.  Re-initialize session token state
		isSessionToken = isSessionTokenCookie();
	}
}
