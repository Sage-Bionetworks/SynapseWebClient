package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;

/**
 * Abstraction for an AsynchronousJobTracker.
 * 
 * @author John
 *
 */
public interface AsynchronousJobTracker {

	/**
	 * For each run, configure must be called with the data to track.
	 * @param toTrack The status to track.
	 * @param waitTimeMS The amount of time between progress checks.
	 * @param handler
	 */
	public void configure(AsynchronousJobStatus toTrack, int waitTimeMS, UpdatingAsynchProgressHandler handler);
	
	/**
	 * Call to start tracking the progress of a job.
	 */
	public void start();
	
	/**
	 * Call to cancel tracking the progress of a job.
	 */
	public void cancel();
}
