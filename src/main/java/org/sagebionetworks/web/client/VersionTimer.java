package org.sagebionetworks.web.client;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;

public class VersionTimer implements SynapseView {

	private static final int CHECK_VERSIONS_INTERVAL_MS = 300000; //5 minutes
	Timer timer;
	GlobalApplicationState globalApplicationState;
	SynapseClientAsync synapseClient;
	
	@Inject
	public VersionTimer(final GlobalApplicationState globalApplicationState, final SynapseClientAsync synapseClient) {
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		globalApplicationState.checkVersionCompatibility(synapseClient, this);
	}
	
	public void start() {
		final SynapseView view = this; 
		timer  = new Timer() {			
			@Override
			public void run() {
				globalApplicationState.checkVersionCompatibility(synapseClient, view);
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
		DisplayUtils.showError(DisplayConstants.NEW_VERSION_AVAILABLE, message);
	}

	@Override
	public void clear() { }
}
