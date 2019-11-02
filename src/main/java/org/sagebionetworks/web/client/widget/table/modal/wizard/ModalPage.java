package org.sagebionetworks.web.client.widget.table.modal.wizard;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a single page of a multiple page modal dialog.
 * 
 * @author jhill
 *
 */
public interface ModalPage extends IsWidget {


	/**
	 * Abstraction for communicating with the containing modal dialog.
	 */
	public interface ModalPresenter {
		/**
		 * Called when a page is finished and the next page should be shown.
		 * 
		 * @param next The next page to show.
		 */
		public void setNextActivePage(ModalPage next);

		/**
		 * Set the primary button's loading state.
		 */
		public void setLoading(boolean loading);

		/**
		 * Set the text of the primary button.
		 * 
		 * @param text
		 */
		public void setPrimaryButtonText(String text);

		/**
		 * Set the instruction message.
		 * 
		 * @param message
		 */
		public void setInstructionMessage(String message);

		/**
		 * Set the error message.
		 * 
		 * @param message
		 */
		void setErrorMessage(String message);

		/**
		 * Set the error
		 * 
		 * @param message
		 */
		void setError(Throwable error);

		void clearErrors();


		/**
		 * Uses selected cancel.
		 */
		public void onCancel();

		/**
		 * Called when the process is finished.
		 * 
		 * @param table
		 */
		public void onFinished();
	}

	/**
	 * Called when the primary button is clicked.
	 */
	public void onPrimary();

	/**
	 * Bind this page to its presenter.
	 * 
	 * @param presenter
	 */
	public void setModalPresenter(ModalPresenter presenter);
}
