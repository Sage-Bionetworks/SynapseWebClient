package org.sagebionetworks.web.client.widget.upload;

/**
 * Abstraction to handle file upload events.
 * 
 * @author jhill
 *
 */
public interface FileUploadHandler {

	/**
	 * Called upon a successful file upload.
	 * 
	 * @param uploadedFile The ID and meta data bundled of the newly created file handle.  This is the result of the upload.
	 */
	void uploadSuccess(UploadedFile uploadedFile);

	/**
	 * Called if an upload fails for any reason.
	 * 
	 * @param error Description of the failure.
	 */
	void uploadFailed(String error);

}
