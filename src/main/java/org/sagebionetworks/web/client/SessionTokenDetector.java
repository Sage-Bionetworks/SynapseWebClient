package org.sagebionetworks.web.client;

import java.util.Objects;

import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;

public class SessionTokenDetector {

	private static final int INTERVAL_MS = 1000*5; //check every 5 seconds
	Timer timer;
	CookieProvider cookies;
	AuthenticationController authController;
	GlobalApplicationState globalAppState;
	boolean isSessionToken;
	@Inject
	public SessionTokenDetector(
			CookieProvider cookies, 
			AuthenticationController authController,
			GlobalApplicationState globalAppState) {
		this.cookies = cookies;
		this.authController = authController;
		this.globalAppState = globalAppState;
		initializeSessionTokenState();
	}
	
	private boolean isSessionTokenCookie() {
		return cookies.getCookie(CookieKeys.USER_LOGIN_TOKEN) != null;
	}
	public void start() {
		timer  = new Timer() {	
			@Override
			public void run() {
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
			}
		};
		timer.scheduleRepeating(INTERVAL_MS);		
	}
	
	public void initializeSessionTokenState() {
		//if user explicitly logs out or in, then there's no need to reload when detecting state change.  Re-initialize session token state
		isSessionToken = isSessionTokenCookie();
	}
}
