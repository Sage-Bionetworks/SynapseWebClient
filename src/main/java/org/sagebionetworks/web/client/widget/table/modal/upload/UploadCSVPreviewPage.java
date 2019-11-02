package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;


/**
 * Abstraction for a page that shows a preview of what a table upload will look like with the option
 * to change how the CSV is read.
 * 
 * @author John
 *
 */
public interface UploadCSVPreviewPage extends ModalPage {

	/**
	 * Call configure before using.
	 * 
	 * @param type The type of the file.
	 * @param fileName The name of the file.
	 * @param parentId The ID of the project that will be the parent of the table.
	 * @param fileHandleId The ID of the FileHandle that the user already uploaded.
	 * @param tableId When provided, the uploaded file will be used to update this table. Can be null.
	 */
	void configure(ContentTypeDelimiter type, String fileName, String parentId, String fileHandleId, String tableId);

}
