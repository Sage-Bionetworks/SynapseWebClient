package org.sagebionetworks.web.unitclient.widget.entity.download;

import org.sagebionetworks.web.client.widget.entity.download.FileUploadHandler;
import org.sagebionetworks.web.client.widget.entity.download.MultipartUploader;

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
	public void uploadSelectedFile(String fileInputId, FileUploadHandler handler) {
		respond(handler);
	}

	@Override
	public void uploadFile(String fileName, String fileInputId, int fileIndex,
			FileUploadHandler handler) {
		respond(handler);
	}
	
	/**
	 * Respond to the handler.
	 * @param handler
	 */
	private void respond(FileUploadHandler handler){
		if(this.progressText != null){
			int index = 0;
			for(String message: progressText){
				handler.updateProgress(index++, message);
			}
		}
		if(error != null){
			handler.uploadFailed(error);
		}else{
			handler.setFileHandleId(fileHandle);
		}
	}

	public void setFileHandle(String fileHandle) {
		this.fileHandle = fileHandle;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setProgressText(String[] progressText) {
		this.progressText = progressText;
	}

}
