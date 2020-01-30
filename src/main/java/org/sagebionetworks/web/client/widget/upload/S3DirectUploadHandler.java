package org.sagebionetworks.web.client.widget.upload;

public interface S3DirectUploadHandler {
	/**
	 * Report progress back
	 * 
	 * @param currentProgress
	 */
	void updateProgress(double currentProgress);

	/**
	 * Report successful upload
	 */
	void uploadSuccess();

	/**
	 * Report upload failed, with a description of the error
	 */
	void uploadFailed(String error);
}
