package org.sagebionetworks.web.client;

import java.util.Objects;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import com.google.inject.Inject;

public class SessionDetector {

	public static final int INTERVAL_MS = 1000 * 10; // check every 10 seconds
	ClientCache clientCache;
	AuthenticationController authController;
	GlobalApplicationState globalAppState;
	GWTWrapper gwt;
	public static final String SESSION_MARKER = "SESSION_MARKER";
	public static final int FORCE_CHECK_EVERY_X_ITERATIONS = 7;
	private int loopCount = 0;

	@Inject
	public SessionDetector(AuthenticationController authController, GlobalApplicationState globalAppState, GWTWrapper gwt, ClientCache clientCache) {
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
		// SWC-4947: check for user change immediately (don't wait 10 seconds to discover the existing
		// session token).
		authController.checkForUserChange();
		checkForUserChangeLater();
	}

	public void checkForUserChangeLater() {
		gwt.scheduleExecution(() -> {
			// SWC-5037: regardless of the current browser state, validate the session token once in a while (in case the current session was removed in another client/instance)
			loopCount++;
			// compare the local user id to the one in the cache (across the current browser tabs)
			String currentSessionUserId = getSessionMarker();
			if ((authController.isLoggedIn() && loopCount%FORCE_CHECK_EVERY_X_ITERATIONS == 0) || !Objects.equals(authController.getCurrentUserPrincipalId(), currentSessionUserId)) {
				// session token state mismatch, update the app state by reloading (refresh leads to auth controller
				// detecting app reload)
				authController.checkForUserChange();
			}
			// continue this loop until a new build is deployed (then stop checking for a user change in this window)
			if (!globalAppState.isShowingVersionAlert()) {
				checkForUserChangeLater();
			}
		}, INTERVAL_MS);
	}
	
	public void initializeSessionTokenState() {
		// Re-initialize session token state
		if (authController.isLoggedIn()) {
			// when a session is given, keep state until logout (or invalid session token is detected)
			clientCache.put(SESSION_MARKER, authController.getCurrentUserPrincipalId(), DateTimeUtilsImpl.getYearFromNow().getTime());
		} else {
			clientCache.remove(SESSION_MARKER);
		}
	}
}
