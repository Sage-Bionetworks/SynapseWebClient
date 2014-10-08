package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a widget that creates and manages the configuration of a CSV upload.
 * @author John
 *
 */
public interface UploadCSVConfigurationWidget extends IsWidget{

	/**
	 * Call configure before using.
	 * @param type The type of the file.
	 * @param fileName The name of the file.
	 * @param parentId The ID of the project that will be the parent of the table.
	 * @param fileHandleId The ID of the FileHandle that the user already uploaded.
	 * @param Handler for 
	 */
	void configure(ContentTypeDelimiter type, String fileName, String parentId, String fileHandleId, PreviewUploadHandler handler);

}
