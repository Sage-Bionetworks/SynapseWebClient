package org.sagebionetworks.web.client.widget.upload;

/**
 * Abstraction for a multi-part files upload handler.
 * 
 * @author John
 *
 */
public interface ProgressingFileUploadHandler extends FileUploadHandler {

	/**
	 * Called as the file upload progresses.
	 * 
	 * @param currentProgress The percent complete.
	 * @param uploadSpeed calculated upload speed
	 */
	void updateProgress(double currentProgress, String progressText, String uploadSpeed);

}
