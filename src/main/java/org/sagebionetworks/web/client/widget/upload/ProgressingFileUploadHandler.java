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
	 * @param progressText A progress message.
	 */
	void updateProgress(double currentProgress, String progressText);

}
