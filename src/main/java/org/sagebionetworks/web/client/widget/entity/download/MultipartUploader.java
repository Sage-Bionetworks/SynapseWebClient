package org.sagebionetworks.web.client.widget.entity.download;

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
	void uploadSelectedFile(String fileInputId, FileUploadHandler handler);
	
	/**
	 * Upload a single file using multi-part upload.
	 * 
	 * @param fileName
	 * @param inputFile
	 * @param handler
	 */
	void uploadFile(String fileName, String fileInputId, int fileIndex, FileUploadHandler handler);

}
