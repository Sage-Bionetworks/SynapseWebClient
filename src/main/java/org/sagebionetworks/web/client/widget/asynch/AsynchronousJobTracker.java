package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;

/**
 * Abstraction for an AsynchronousJobTracker.
 * 
 * @author John
 * 
 */
public interface AsynchronousJobTracker {

	/**
	 * Start a new Asynchronous Job from the passed AsynchronousRequestBody and
	 * track the progress of the job until completion.s
	 * 
	 * @param requestBody
	 * @param waitTimeMS
	 * @param handler
	 */
	public void start(AsynchronousRequestBody requestBody, int waitTimeMS,
			UpdatingAsynchProgressHandler handler);

	/**
	 * Call to cancel tracking the progress of a job.
	 */
	public void cancel();
}
