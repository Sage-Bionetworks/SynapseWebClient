package org.sagebionetworks.web.unitclient.widget.upload;

import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;

/**
 * Test helper for MultipartUploader.
 * 
 * @author John
 *
 */
public class MultipartUploaderStub implements MultipartUploader {

	String fileHandle;
	String error;
	String[] progressText;
	String[] uploadSpeed;
	boolean isCanceled = false;

	/**
	 * Respond to the handler.
	 * 
	 * @param handler
	 */
	private void respond(ProgressingFileUploadHandler handler) {
		if (this.progressText != null) {
			int index = 0;
			for (String message : progressText) {
				String currentUploadSpeed = uploadSpeed[index];
				handler.updateProgress(index++, message, currentUploadSpeed);
			}
		}
		if (error != null) {
			handler.uploadFailed(error);
		} else {
			handler.uploadSuccess(fileHandle);
		}
	}

	public void setFileHandle(String fileHandle) {
		this.fileHandle = fileHandle;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setProgressText(String... progressText) {
		this.progressText = progressText;
	}

	public void setUploadSpeed(String... uploadSpeed) {
		this.uploadSpeed = uploadSpeed;
	}

	@Override
	public void uploadFile(String fileName, String contentType, JavaScriptObject blob, ProgressingFileUploadHandler handler, Long storageLocationId, HasAttachHandlers view) {
		respond(handler);
	}

	@Override
	public void cancelUpload() {
		isCanceled = true;
	}

	public boolean isCanceled() {
		return isCanceled;
	}
}
