package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * An abstraction for a widget that is composed of only a file input form.
 * It can be used to upload the selected files into file handles.
 *  
 * @author jhill
 *
 */
public interface FileInputWidget extends IsWidget {
	
	/**
	 * Configure this widget before use.
	 */
	public void configure(FileUploadHandler handler);
	
	/**
	 * Upload the selected files.
	 */
	public void uploadSelectedFile();

}
