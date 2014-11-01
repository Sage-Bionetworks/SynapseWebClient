package org.sagebionetworks.web.client.widget.table.modal;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A simple model dialog for creating a table.
 * 
 * @author jhill
 *
 */
public interface CreateTableModalView extends IsWidget {
	
	/**
	 * Business logic handler for this view.
	 *
	 */
	public interface Presenter {
		
		/**
		 * Called when the create button is pressed.
		 */
		public void onCreateTable();
	}

	
	public String getTableName();
	
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
