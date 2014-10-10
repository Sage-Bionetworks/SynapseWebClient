package org.sagebionetworks.web.client.widget.table.modal.upload;


/**
 * Abstraction for a page that manages the configuration of the table including, table name, columns, and CSV descriptors..
 * @author John
 *
 */
public interface UploadCSVConfigurationPage extends ModalPage {

	/**
	 * Call configure before using.
	 * @param type The type of the file.
	 * @param fileName The name of the file.
	 * @param parentId The ID of the project that will be the parent of the table.
	 * @param fileHandleId The ID of the FileHandle that the user already uploaded.
	 */
	void configure(ContentTypeDelimiter type, String fileName, String parentId, String fileHandleId);

}
