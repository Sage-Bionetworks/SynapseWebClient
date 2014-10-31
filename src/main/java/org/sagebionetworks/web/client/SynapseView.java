package org.sagebionetworks.web.client;

public interface SynapseView extends ShowsErrors {

	/**
	 * Shows a loading view
	 */
	public void showLoading();
	
	/**
	 * Shows user info
	 * @param title
	 * @param message
	 */
	public void showInfo(String title, String message);
	

	
	/**
	 * Clears out old elements
	 */
	public void clear();
	
}
