package org.sagebionetworks.web.client.widget.upload;

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
	void uploadSelectedFile(String fileInputId, ProgressingFileUploadHandler handler);
	
	/**
	 * Upload a single file using multi-part upload.
	 * 
	 * @param fileName
	 * @param inputFile
	 * @param handler
	 */
	void uploadFile(String fileName, String fileInputId, int fileIndex, ProgressingFileUploadHandler handler);
	

	/**
	 * Get the metadata about the selected files
	 * 
	 * @param inputId
	 * @return
	 */
	FileMetadata[] getSelectedFileMetadata(String inputId);

}
