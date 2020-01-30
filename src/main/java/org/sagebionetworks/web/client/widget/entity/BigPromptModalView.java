package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * A simple model dialog to ask for multiline text from the user
 *
 */
public interface BigPromptModalView extends IsWidget {

	/**
	 * Configure this view with starting data.
	 * 
	 * @param title The modal title
	 * @param label The label.
	 * @param value The initial value.
	 */
	public void configure(String title, String label, String value, Callback callback);

	public void configure(String title, String label, String value);

	public String getValue();

	/**
	 * Show an error message..
	 * 
	 * @param error
	 */
	public void showError(String error);

	/**
	 * Show the dialog.
	 */
	public void show();

	/**
	 * Hide the dialog.
	 */
	public void hide();

	/**
	 * Clear name and errors.
	 */
	public void clear();

	/**
	 * Set loading state.
	 * 
	 * @param isLoading
	 */
	public void setLoading(boolean isLoading);

	void addStyleToModal(String styles);

	void setTextAreaHeight(String height);

}
