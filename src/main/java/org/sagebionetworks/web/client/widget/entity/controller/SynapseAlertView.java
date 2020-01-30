package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstraction for the view
 *
 */
public interface SynapseAlertView extends IsWidget {
	/**
	 * Clears all state in the view (makes all components invisible).
	 */
	void clearState();

	/**
	 * Show the error in an alert.
	 * 
	 * @param error
	 */
	void showError(String error);

	/**
	 * Show a special login alert, that includes a link to the login page.
	 */
	void showLogin();

	void setLoginWidget(Widget w);

	void reload();

	void setRetryButtonVisible(boolean visible);
}
