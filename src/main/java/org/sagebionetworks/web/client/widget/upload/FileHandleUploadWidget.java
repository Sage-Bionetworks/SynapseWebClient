package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * A one-click file handle upload widget.
 * 
 * @author John
 *
 */
public interface FileHandleUploadWidget extends IsWidget {

	public void reset();

	FileMetadata[] getSelectedFileMetadata();

	void setUploadingCallback(Callback startedUploadingCallback);

	void configure(String buttonText, CallbackP<FileUpload> finishedUploadingCallback);

	void setValidation(FileValidator validator);

	void setUploadedFileText(String text);

	void allowMultipleFileUpload(boolean value);
}
