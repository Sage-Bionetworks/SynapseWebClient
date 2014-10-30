package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.ShowsErrors;
import org.sagebionetworks.web.client.utils.Callback;
/**
 * Abstraction for the view 
 * @author John
 *
 */
public interface EntityActionControllerView extends ShowsErrors {
	

	/**
	 * Show the user a confirm dialog.
	 * @param string
	 * @param action
	 */
	void showConfirmDialog(String title, String string, Callback callback);
	

	/**
	 * Show info to the user.
	 * @param string
	 * @param string
	 */
	void showInfo(String header, String message);

}
