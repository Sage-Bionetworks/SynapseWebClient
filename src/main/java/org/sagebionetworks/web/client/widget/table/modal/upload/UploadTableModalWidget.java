package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;

/**
 * Abstraction for a widget used to upload CSV files to a table.
 * 
 * This widget is a modal dialog and must be added to the page.
 * 
 * @author John
 *
 */
public interface UploadTableModalWidget extends ModalWizardWidget {

	/**
	 * Configure this widget before using it.
	 * 
	 * @param parentId
	 * @param handler Table creation notification.
	 */
	public void configure(String parentId);
	
}
