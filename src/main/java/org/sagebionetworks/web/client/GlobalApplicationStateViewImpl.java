package org.sagebionetworks.web.client;

public class GlobalApplicationStateViewImpl implements
		GlobalApplicationStateView {
	private static final int TEN_MINUTES = 1000*60*10; //10 minutes
	
	@Override
	public void showVersionOutOfDateGlobalMessage() {
		DisplayUtils.showError(DisplayConstants.NEW_VERSION_AVAILABLE, DisplayConstants.NEW_VERSION_INSTRUCTIONS, TEN_MINUTES);
	}
}
