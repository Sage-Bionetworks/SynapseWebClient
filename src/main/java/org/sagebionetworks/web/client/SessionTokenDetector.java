package org.sagebionetworks.web.client;

import java.util.Objects;

import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.core.shared.GWT;
import com.google.inject.Inject;

public class SessionTokenDetector {

	public static final int INTERVAL_MS = 1000*10; //check every 10 seconds
	ClientCache clientCache;
	AuthenticationController authController;
	GlobalApplicationState globalAppState;
	GWTWrapper gwt;
	boolean isSessionMarker;
	public static final String SESSION_MARKER = "SESSION_MARKER";
	@Inject
	public SessionTokenDetector(
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
	
	private boolean isSessionMarkerSet() {
		return clientCache.contains(SESSION_MARKER);
	}
	
	public void start() {
		gwt.scheduleFixedDelay(() -> {
			boolean isNowSessionToken = isSessionMarkerSet();
			if (!Objects.equals(isSessionMarker, isNowSessionToken)) {
				// session token state mismatch, update the app state by refreshing the page
				globalAppState.refreshPage();
			}
		}, INTERVAL_MS);
	}
	
	public void initializeSessionTokenState() {
		//Re-initialize session token state
		if (authController.isLoggedIn()) {
			// when a session is given, keep state for a day (less if logout or invalid session token is detected)
			clientCache.put(SESSION_MARKER, Boolean.TRUE.toString(), 1000L*60L*60L*24L);
		} else {
			clientCache.remove(SESSION_MARKER);
		}
		isSessionMarker = isSessionMarkerSet();
	}
}
