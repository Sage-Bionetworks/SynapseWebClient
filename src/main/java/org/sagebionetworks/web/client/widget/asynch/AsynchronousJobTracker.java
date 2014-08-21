package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;

/**
 * Abstraction for an AsynchronousJobTracker.
 * 
 * @author John
 *
 */
public interface AsynchronousJobTracker {

	public void configure(AsynchronousJobStatus toTrack, int waitTimeMS, UpdatingAsynchProgressHandler handler);
	
	public void start();
	
	public void cancel();
}
