package org.sagebionetworks.web.client;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;

public class VersionTimer {

	private static final int CHECK_VERSIONS_INTERVAL_MS = 1000 * 60 * 10; // 10 minutes
	Timer timer;
	GlobalApplicationState globalApplicationState;

	@Inject
	public VersionTimer(final GlobalApplicationState globalApplicationState) {
		this.globalApplicationState = globalApplicationState;
		globalApplicationState.checkVersionCompatibility(null);
	}

	public void start() {
		timer = new Timer() {
			@Override
			public void run() {
				globalApplicationState.checkVersionCompatibility(null);
			}
		};
		timer.scheduleRepeating(CHECK_VERSIONS_INTERVAL_MS);
	}
}
