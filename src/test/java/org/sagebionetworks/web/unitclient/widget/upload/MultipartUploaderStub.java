package org.sagebionetworks.web.unitclient.widget.upload;

import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.client.widget.upload.UploadedFile;

/**
 * Test helper for MultipartUploader.
 * @author John
 *
 */
public class MultipartUploaderStub implements MultipartUploader {
	
	String fileHandle;
	String error;
	String[] progressText;

	@Override
	public void uploadSelectedFile(String fileInputId, ProgressingFileUploadHandler handler, Long storageLocationId) {
		respond(handler);
	}

	@Override
	public void uploadFile(String fileName, String fileInputId, int fileIndex,
			ProgressingFileUploadHandler handler, Long storageLocationId) {
		respond(handler);
	}
	
	/**
	 * Respond to the handler.
	 * @param handler
	 */
	private void respond(ProgressingFileUploadHandler handler){
		if(this.progressText != null){
			int index = 0;
			for(String message: progressText){
				handler.updateProgress(index++, message);
			}
		}
		if(error != null){
			handler.uploadFailed(error);
		}else{
			handler.uploadSuccess(new UploadedFile(null, fileHandle));
		}
	}

	public void setFileHandle(String fileHandle) {
		this.fileHandle = fileHandle;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setProgressText(String...progressText) {
		this.progressText = progressText;
	}

	@Override
	public FileMetadata[] getSelectedFileMetadata() {
		// TODO Auto-generated method stub
		return null;
	}


}
