package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface EditProjectMetadataModalView extends IsWidget {

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

	public void configure(String entityName, String alias);

	public String getAlias();

	public String getEntityName();

	public void setAliasUIVisible(boolean visible);

	/**
	 * Show an error message..
	 * 
	 * @param error
	 */
	public void showError(String error);

	/**
	 * Bind this view to its presenter.
	 * 
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
	 * 
	 * @param isLoading
	 */
	public void setLoading(boolean isLoading);

}
