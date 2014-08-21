package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;

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
	public void onCancel(AsynchronousJobStatus status);
	
	/**
	 * Called when the job completes with either a success or failure.
	 * 
	 * @param status
	 */
	public void onComplete(AsynchronousJobStatus status);
	
	/**
	 * Called when checking the status fails, this is not the same as a job failure.
	 * 
	 * @param failure
	 */
	public void onStatusCheckFailure(String jobId, Throwable failure);
}
