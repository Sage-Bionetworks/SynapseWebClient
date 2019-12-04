package org.sagebionetworks.web.client.widget.entity;

import java.util.List;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * A simple model dialog prompting for multiple string values
 *
 */
public interface PromptForValuesModalView extends IsWidget {

	public void configureAndShow(String title, String prompt, String initialValue, CallbackP<String> newValueCallback);

	/**
	 * Configure this view with starting data.
	 * 
	 * @param title The modal title
	 * @param prompts Prompt values
	 * @param initialValues Initial values.
	 * @param newValuesCallback Call back containing new values
	 */
	public void configureAndShow(String title, List<String> prompts, List<String> initialValues, CallbackP<List<String>> newValuesCallback);

	/**
	 * Show an error message..
	 * 
	 * @param error
	 */
	public void showError(String error);

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

	public void hide();
}
