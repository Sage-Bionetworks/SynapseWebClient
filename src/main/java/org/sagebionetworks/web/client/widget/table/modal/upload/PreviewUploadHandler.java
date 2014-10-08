package org.sagebionetworks.web.client.widget.table.modal.upload;

/**
 * 
 * @author John
 *
 */
public interface PreviewUploadHandler {
	
	/**
	 * Called when uploads fails.
	 * 
	 * @param message
	 */
	public void uploadFailed(String message);
	
	/**
	 * Called to indicate a loading state.
	 * @param loading True when loading, else false.
	 */
	public void setLoading(boolean loading);

	/**
	 * Called if the user cancels.
	 */
	public void onCancel();

}
