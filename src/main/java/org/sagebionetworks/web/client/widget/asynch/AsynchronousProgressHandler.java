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
	 * 
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
	 * Called when a job fails or if some other errors occurs.
	 * 
	 * @param failure
	 */
	public void onFailure(Throwable failure);
}
