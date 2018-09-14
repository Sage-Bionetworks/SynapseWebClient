package org.sagebionetworks.web.client;

public interface SynapseView extends ShowsErrors {

	/**
	 * Shows a loading view
	 */
	public void showLoading();
	
	/**
	 * Shows user info
	 * @param message
	 */
	public void showInfo(String message);
	

	
	/**
	 * Clears out old elements
	 */
	public void clear();
	
}
