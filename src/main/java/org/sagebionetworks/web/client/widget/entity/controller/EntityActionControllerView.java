package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.web.client.ShowsErrors;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * Abstraction for the view 
 * @author John
 *
 */
public interface EntityActionControllerView extends ShowsErrors, IsWidget {
	

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
	 * Show info dialog to the user.
	 */
	void showInfoDialog(String header, String message);

	/**
	 * This dialog must be added to the page.
	 * @param accessControlListModalWidget
	 */
	void addAccessControlListModalWidget(IsWidget accessControlListModalWidget);
	
	/**
	 * This dialog must be added to the page.
	 * @param modalWidget
	 */
	void addMarkdownEditorModalWidget(IsWidget modalWidget);

	/**
	 * Prompt the user to enter a string value.
	 * @param prompt
	 * @param callback
	 */
	void showPromptDialog(String prompt, PromptCallback callback);
}
