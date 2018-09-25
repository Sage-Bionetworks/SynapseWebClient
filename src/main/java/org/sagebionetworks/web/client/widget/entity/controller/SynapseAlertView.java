package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
/**
 * Abstraction for the view 
 *
 */
public interface SynapseAlertView extends IsWidget {
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
	 * Show info about the jira issue that was just created.
	 */
	void showJiraIssueOpen(String key, String url);
	
	
	/**
	 * Show a special login alert, that includes a link to the login page.
	 */
	void showLogin();
	void setPresenter(Presenter p);
	void setLoginWidget(Widget w);
	
	void reload();
	void setRetryButtonVisible(boolean visible);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onCreateJiraIssue(String userReport);
	}
}
