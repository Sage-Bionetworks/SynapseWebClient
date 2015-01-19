package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A simple model dialog selecting an entity name either to create an entity or rename it.
 * 
 * @author jhill
 *
 */
public interface EntityNameModalView extends IsWidget {
	
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
	 * @param label The label of the name button.
	 * @param buttonText The text of the primary button.
	 * @param name The value of the name field.
	 */
	public void configure(String title, String label, String buttonText, String name);
	
	public String getName();
	
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
