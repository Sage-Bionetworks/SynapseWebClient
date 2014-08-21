package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;

/**
 * An AsynchronousProgressHandler that can also receive updates.
 * 
 * @author jmhill
 *
 */
public interface UpdatingAsynchProgressHandler extends
		AsynchronousProgressHandler {

	public void onUpdate(AsynchronousJobStatus status);
}
