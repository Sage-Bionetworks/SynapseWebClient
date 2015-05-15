package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.ShowsErrors;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * Abstraction for the view 
 *
 */
public interface SynapseAlertView extends IsWidget {
	
	/**
	 * Show info to the user.
	 * @param string
	 * @param string
	 */
	void showInfo(String header, String message);
	
	/**
	 * Gather information from the user to create a jira issue
	 * @param errorMessage
	 */
	void showJiraDialog(String errorMessage);
	
	/**
	 * Instruct the view to hide the jira issue creation dialog.
	 */
	void hideJiraDialog();
	
	/**
	 * Clears all state in the view (makes all components invisible).
	 */
	void clearState();
	
	/**
	 * Show the error in an alert.
	 * @param error
	 */
	void showError(String error);
	
	/**
	 * Show a special login alert, that includes a link to the login page.
	 */
	void showLoginAlert();
	
	void setPresenter(Presenter p);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onCreateJiraIssue(String userReport);
		void onLoginClicked();
	}
}
