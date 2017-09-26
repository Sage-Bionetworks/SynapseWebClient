package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;

/**
 * Abstraction for a muti-part file uploader.
 * 
 * @author Jay
 *
 */
public interface MultipartUploader {

	/**
	 * Upload a single file using multi-part upload.
	 * If there are no files selected an error will be shown.
	 * 
	 * @param fileInputId
	 * @param fileIndex
	 * @param handler
	 */
	void uploadSelectedFile(String fileInputId, ProgressingFileUploadHandler handler, Long storageLocationId, HasAttachHandlers view);
	
	void uploadFile(String fileInputId, int fileIndex, ProgressingFileUploadHandler handler, Long storageLocationId, HasAttachHandlers view);
	
	/**
	 * Upload a single file using multi-part upload.
	 * 
	 * @param fileName
	 * @param inputFile
	 * @param handler
	 */
	void uploadFile(String fileName, String contentType, JavaScriptObject blob, ProgressingFileUploadHandler handler, Long storageLocationId, HasAttachHandlers view);
	void cancelUpload();
}
