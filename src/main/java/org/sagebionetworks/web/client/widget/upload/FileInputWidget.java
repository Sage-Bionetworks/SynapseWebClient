package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * An abstraction for a widget that is composed of only a file input form.
 * It can be used to upload the selected files into file handles.
 *  
 * @author jhill
 *
 */
@Deprecated // Use org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget
public interface FileInputWidget extends IsWidget {
	
	
	/**
	 * Upload the selected files.
	 * @param handler Will be notified of file upload success or failure.
	 */
	public void uploadSelectedFile(FileUploadHandler handler);
	
	/**
	 * Reset the widget to clear all state.
	 */
	public void reset();

}
