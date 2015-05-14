package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.UploadedFile;

import com.google.gwt.user.client.ui.Widget;

/**
 * Helper for testing.
 * @author John
 *
 */
public class FileInputWidgetStub implements FileInputWidget{
	
	String errorString;
	String fileHandle;
	FileMetadata[] metadata;

	@Override
	public Widget asWidget() {
		return null;
	}

	@Override
	public void uploadSelectedFile(FileUploadHandler handler) {
		if(errorString != null){
			handler.uploadFailed(errorString);
		}else if(fileHandle != null){
			handler.uploadSuccess(new UploadedFile(null, fileHandle));
		}
	}

	@Override
	public void reset() {
	}

	@Override
	public FileMetadata[] getSelectedFileMetadata() {
		return metadata;
	}

	public String getErrorString() {
		return errorString;
	}

	/**
	 * Set to cause an error.
	 * @param errorString
	 */
	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}

	public String getFileHandle() {
		return fileHandle;
	}

	/**
	 * Set to cause a success.
	 * @param fileHandle
	 */
	public void setFileHandle(String fileHandle) {
		this.fileHandle = fileHandle;
	}

	/**
	 * Set for the selected file.
	 * @param metadata
	 */
	public void setMetadata(FileMetadata[] metadata) {
		this.metadata = metadata;
	}

	
}
