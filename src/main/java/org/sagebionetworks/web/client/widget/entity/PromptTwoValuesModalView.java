package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A simple model dialog prompting for 2 values
 *
 */
public interface PromptTwoValuesModalView extends IsWidget {
	
	/**
	 * Business logic handler for this view.
	 *
	 */
	public interface Presenter {
		
		/**
		 * Called when the create button is pressed.
		 */
		public void onPrimary();
	}

	/**
	 * Configure this view with starting data.
	 * @param title The modal title
	 * @param label1 The label of the first field.
	 * @param value1 The value of the first field
	 * @param label2 The label of the second field.
	 * @param value2 The value of the second field
	 * @param buttonText The text of the primary button.
	 */
	public void configure(String title, String label1, String value1, String label2, String value2, String buttonText);
	
	public String getValue1();
	public String getValue2();
	
	/**
	 * Show an error message..
	 * @param error
	 */
	public void showError(String error);
	
	/**
	 * Bind this view to its presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
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
	 * @param isLoading
	 */
	public void setLoading(boolean isLoading);

}
