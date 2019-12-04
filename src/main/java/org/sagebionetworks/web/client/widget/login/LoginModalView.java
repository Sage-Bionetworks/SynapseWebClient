package org.sagebionetworks.web.client.widget.login;

import org.gwtbootstrap3.client.ui.ModalSize;
import com.google.gwt.user.client.ui.IsWidget;

public interface LoginModalView extends IsWidget {
	/**
	 * Business logic for this view can be found in the presenter.
	 * 
	 *
	 */
	public interface Presenter {
		/**
		 * Called when the primary button is pressed.
		 */
		void onPrimary();

		void onSubmitComplete(String results);
	}

	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Show the modal dialog.
	 */
	public void showModal();

	/**
	 * Show/hide the error alert.
	 * 
	 * @param b
	 */
	public void showAlert(boolean visible);

	/**
	 * Show an error message.
	 * 
	 * @param error
	 */
	public void showErrorMessage(String error);

	/**
	 * Show an error message.
	 * 
	 * @param error
	 */
	public void showErrorMessagePopup(String error);

	/**
	 * Change the state of the primary button.
	 * 
	 * @param enabled
	 */
	public void setLoading(boolean enabled);

	/**
	 * Hide the modal dialog.
	 */
	public void hideModal();

	/**
	 * Set the text of the primary button.
	 * 
	 * @param text
	 */
	public void setPrimaryButtonText(String text);

	/**
	 * Set the modal title.
	 * 
	 * @param title
	 */
	public void setTitle(String title);

	/**
	 * Set the modal size.
	 * 
	 * @param size
	 */
	public void setSize(ModalSize size);

	/**
	 * Set the instructions for the login dialog form
	 * 
	 * @param message
	 */
	public void setInstructionsMessage(String message);

	/**
	 * Set the form action url, method, and submit
	 * 
	 * @param action
	 */
	public void submitForm(String actionUrl, String method, String encodingType);

	void clearForm();
}
