package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.ShowsErrors;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * Abstraction for the view 
 *
 */
public interface SynapseAlertView extends ShowsErrors, IsWidget {
	

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
	
	/**
	 * Gather information from the user to create a jira issue
	 * @param errorMessage
	 */
	void showJiraDialog(String errorMessage);
	void hideJiraDialog();
	void setPresenter(Presenter p);
	
	void clearState();
	void showError(String error);
	void showLoginAlert();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onCreateJiraIssue(String userReport);
		void onLoginClicked();
	}
}
