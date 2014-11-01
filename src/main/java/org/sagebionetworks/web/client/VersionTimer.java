package org.sagebionetworks.web.client;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;

public class VersionTimer implements SynapseView {

	private static final int CHECK_VERSIONS_INTERVAL_MS = 1000*60*10; //10 minutes
	Timer timer;
	GlobalApplicationState globalApplicationState;
	
	@Inject
	public VersionTimer(final GlobalApplicationState globalApplicationState) {
		this.globalApplicationState = globalApplicationState;
		globalApplicationState.checkVersionCompatibility(this);
	}
	
	public void start() {
		final SynapseView view = this; 
		timer  = new Timer() {			
			@Override
			public void run() {
				globalApplicationState.checkVersionCompatibility(view);
			}
		};
		timer.scheduleRepeating(CHECK_VERSIONS_INTERVAL_MS);		
	}

	@Override
	public void showLoading() {	}

	@Override
	public void showInfo(String title, String message) { }

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showError(DisplayConstants.NEW_VERSION_AVAILABLE, message, CHECK_VERSIONS_INTERVAL_MS);
	}

	@Override
	public void clear() { }
}
