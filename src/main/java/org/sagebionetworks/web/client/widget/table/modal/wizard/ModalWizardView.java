package org.sagebionetworks.web.client.widget.table.modal.wizard;

import org.gwtbootstrap3.client.ui.ModalSize;

import com.google.gwt.user.client.ui.IsWidget;

public interface ModalWizardView extends IsWidget {
	/**
	 * Business logic for this view can be found in the presenter.
	 * 
	 * @author John
	 *
	 */
	public interface Presenter {
		/**
		 * Called when the primary button is pressed.
		 */
		void onPrimary();

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
	 * Set the widget for the body.
	 * 
	 * @param body
	 */
	public void setBody(IsWidget body);

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
	 * Set the instructions message.
	 * 
	 * @param string
	 */
	public void setInstructionsMessage(String message);

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
	 * @param title
	 */
	public void setTile(String title);

	/**
	 * Set the modal size.
	 * @param size
	 */
	public void setSize(ModalSize size);

	void setHelp(String helpMarkdown, String helpUrl);
}
