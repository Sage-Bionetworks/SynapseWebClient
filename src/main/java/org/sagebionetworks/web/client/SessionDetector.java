package org.sagebionetworks.web.client;

import java.util.Objects;

import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

public class SessionDetector {

	public static final int INTERVAL_MS = 1000*10; //check every 10 seconds
	ClientCache clientCache;
	AuthenticationController authController;
	GlobalApplicationState globalAppState;
	GWTWrapper gwt;
	public static final String SESSION_MARKER = "SESSION_MARKER";
	@Inject
	public SessionDetector(
			AuthenticationController authController,
			GlobalApplicationState globalAppState,
			GWTWrapper gwt,
			ClientCache clientCache) {
		this.clientCache = clientCache;
		this.authController = authController;
		this.globalAppState = globalAppState;
		this.gwt = gwt;
		initializeSessionTokenState();
	}
	
	private String getSessionMarker() {
		return clientCache.get(SESSION_MARKER);
	}
	
	public void start() {
		gwt.scheduleFixedDelay(() -> {
			// compare the local user id to the one in the cache (across browser, current value)
			String currentSessionUserId = getSessionMarker();
			if (!Objects.equals(authController.getCurrentUserPrincipalId(), currentSessionUserId)) {
				// session token state mismatch, update the app state by reloading (refresh leads to auth controller detecting app reload)
				Window.Location.reload();
			}
		}, INTERVAL_MS);
	}
	
	public void initializeSessionTokenState() {
		//Re-initialize session token state
		if (authController.isLoggedIn()) {
			// when a session is given, keep state until logout (or invalid session token is detected)
			clientCache.put(SESSION_MARKER, authController.getCurrentUserPrincipalId(), DateTimeUtilsImpl.getYearFromNow().getTime());
		} else {
			clientCache.remove(SESSION_MARKER);
		}
	}
}
