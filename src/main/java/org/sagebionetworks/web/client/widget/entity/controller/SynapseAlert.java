package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;


/**
 * This widget can be added to any view, and then asked to handle an error or show a specific error
 * message.
 */
public interface SynapseAlert extends IsWidget {
	/**
	 * Handles the given throwable. Can be used to handle async rpc failures (it recognizes common
	 * Synapse states, like if the system is down).
	 * 
	 * @param t
	 */
	void handleException(Throwable t);

	/**
	 * Tell the widget to display the given error message. This is an alternative to asking the widget
	 * to handle an exception, used when you expect specific exceptions, and can interpret them in the
	 * correct context.
	 * 
	 * @param error
	 */
	void showError(String error);

	/**
	 * Called to instruct the widget to ask the user to login to access the resource (with a link to the
	 * login page). Can be called when you know that the user must be logged in in order to use the
	 * widget.
	 */
	void showLogin();

	/**
	 * Convenience method to answer if the user is currently logged in.
	 * 
	 * @return
	 */
	boolean isUserLoggedIn();

	/**
	 * Hide all errors that were previously shown.
	 */
	void clear();

	void consoleError(String errorMessage);
}
