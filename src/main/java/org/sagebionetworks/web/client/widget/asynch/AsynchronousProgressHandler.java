package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;


/**
 * Handler for all job terminations.
 * 
 * @author John
 *
 */
public interface AsynchronousProgressHandler {
	
	/**
	 * Called when the user cancels the job.
	 * @param status
	 */
	public void onCancel();
	
	/**
	 * Called when the job completes with either a success or failure.
	 * 
	 * @param status
	 */
	public void onComplete(AsynchronousResponseBody response);
	
	/**
	 * Called when checking the status fails, this is not the same as a job failure.
	 * @param failure
	 */
	public void onStatusCheckFailure(Throwable failure);
}
