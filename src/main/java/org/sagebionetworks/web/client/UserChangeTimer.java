package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;

public class UserChangeTimer {

	private static final int CHECK_USER_CHANGE_INTERVAL_MS = 1000*50; //check every 50 seconds
	Timer timer;
	AuthenticationController authController;
	
	@Inject
	public UserChangeTimer(final AuthenticationController authController) {
		this.authController = authController;
	}
	
	public void start() {
		timer  = new Timer() {			
			@Override
			public void run() {
				authController.checkForUserChange();
			}
		};
		timer.scheduleRepeating(CHECK_USER_CHANGE_INTERVAL_MS);		
	}
}
