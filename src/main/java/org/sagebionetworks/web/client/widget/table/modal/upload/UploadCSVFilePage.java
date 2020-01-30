package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

/**
 * Abstraction for page that handles the upload of a CSV file.
 * 
 * @author jhill
 *
 */
public interface UploadCSVFilePage extends ModalPage {

	/**
	 * Configure this page before using it.
	 * 
	 * @param parentId The ID of the parent project of the new table.
	 * @param tableId When provided, the uploaded file will be used to update this table. Can be null.
	 */
	void configure(String parentId, String tableId);

}
