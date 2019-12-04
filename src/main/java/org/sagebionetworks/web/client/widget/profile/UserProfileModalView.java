package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for an editor of a user's profile.
 * 
 * @author jhill
 *
 */
public interface UserProfileModalView extends IsWidget {

	public interface Presenter {

		/**
		 * Called when the user clicks save.
		 */
		void onSave();

	}

	/**
	 * Bind the view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Show/hide the loading state of the view.
	 * 
	 * @param loading
	 */
	public void setLoading(boolean loading);

	/**
	 * Show an error message
	 * 
	 * @param message
	 */
	public void showError(String message);

	/**
	 * Show the modal.
	 */
	public void showModal();

	/**
	 * Hide the error message
	 */
	public void hideError();

	/**
	 * Set the primary button to processing.
	 * 
	 * @param b
	 */
	public void setProcessing(boolean b);

	/**
	 * Hide the modal
	 */
	public void hideModal();

	/**
	 * Add the editor to the modal
	 * 
	 * @param editorWidget
	 */
	public void addEditorWidget(IsWidget editorWidget);

}
