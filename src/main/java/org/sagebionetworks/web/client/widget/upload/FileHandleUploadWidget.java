package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * A one-click file handle upload widget.
 * 
 * @author John
 *
 */
public interface FileHandleUploadWidget extends IsWidget{

	/**
	 * Configure the widget before using.
	 * @param callback Will be called with the uploaded fileHandleId if the user successfully uploads a file.
	 */
	public void configure(String buttonText, CallbackP<String> finishedUploadingCallback);
	
	public void configure(String buttonText, Callback uploadInProgress, CallbackP<String> finishedUploadingCallback);

	public void reset();

	FileMetadata[] getFileMetadata();
}
