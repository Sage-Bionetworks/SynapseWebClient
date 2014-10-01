package org.sagebionetworks.web.client.widget.entity.download;

/**
 * Abstraction for a multi-part files upload handler.
 * 
 * @author John
 *
 */
public interface FileUploadHandler {

	/**
	 * Called as the file upload progresses.
	 * 
	 * @param currentProgress The percent complete.
	 * @param progressText A progress message.
	 */
	void updateProgress(double currentProgress, String progressText);

	/**
	 * Called upon a successful file upload.
	 * 
	 * @param fileHandleId The ID of the newly created file handle.  This is the result of the upload.
	 */
	void uploadSuccess(String fileHandleId);

	/**
	 * Called if an upload fails for any reason.
	 * 
	 * @param error Description of the failure.
	 */
	void uploadFailed(String error);

}
