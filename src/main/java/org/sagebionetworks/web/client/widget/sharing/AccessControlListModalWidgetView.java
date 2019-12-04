package org.sagebionetworks.web.client.widget.sharing;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction between the a AccessControlListModalWidget and its view.
 * 
 * @author John
 *
 */
public interface AccessControlListModalWidgetView extends IsWidget {

	interface Presenter {
		/**
		 * Called when the user selects the primary button.
		 */
		public void onPrimary();
	}

	/**
	 * Show the dialog.
	 */
	void showDialog();

	/**
	 * Set the text on the default button (can be "OK" or "Cancel" depending on the state);
	 * 
	 * @param string
	 */
	void setDefaultButtonText(String string);

	/**
	 * Show/hide the primary button.
	 * 
	 * @param b
	 */
	void setPrimaryButtonVisible(boolean visibile);

	/**
	 * Enable/disable the primary button.
	 * 
	 * @param b
	 */
	void setPrimaryButtonEnabled(boolean enabled);

	/**
	 * Add the editor to the dialog.
	 * 
	 * @param editor
	 */
	void addEditor(IsWidget editor);

	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Hide the dialog
	 */
	void hideDialog();

	/**
	 * Set the loading state
	 * 
	 * @param b
	 */
	void setLoading(boolean loading);

	void setTitle(String title);

}
