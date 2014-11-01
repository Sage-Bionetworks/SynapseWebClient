package org.sagebionetworks.web.client.widget.table.modal.wizard;

import org.gwtbootstrap3.client.ui.ModalSize;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * Abstraction for ModalWizardWidget that acts as a modal dialog that shows multiple pages.
 * 
 * @author jhill
 *
 */
public interface ModalWizardWidget extends IsWidget {
	
	/**
	 * Configure this widget before using it.
	 * @param firstPage The first page of the wizard.
	 */
	public void configure(ModalPage firstPage);
	
	/**
	 * Set the title of the main dialog.
	 * @param title
	 */
	void setTitle(String title);
	
	/**
	 * Set the size of the main dialog.
	 * @param size
	 */
	void setModalSize(ModalSize size);
	
	/**
	 * Call this method to start the wizard.
	 * @param callback Handles success calls.
	 */
	void showModal(WizardCallback callback);
	
	/**
	 * Callback for wizard completion events.
	 */
	interface WizardCallback {
		
		/**
		 * Called when the wizard finishes with success.
		 */
		void onFinished();
		
		/**
		 * Called if the wizard is canceled.
		 */
		void onCanceled();
	}
}
