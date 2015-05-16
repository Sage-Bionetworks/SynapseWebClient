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
	
	public void reset();

	FileMetadata[] getSelectedFileMetadata();

	void configureValidation(FileValidator validator,
			Callback validFileCallback);

	void configureUploadingCallback(Callback startedUploadingCallback);

	void configure(String buttonText,
			CallbackP<FileUpload> finishedUploadingCallback);
}
