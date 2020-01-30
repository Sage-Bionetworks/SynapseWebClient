package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a widget that starts and tracks a job.
 * 
 * @author John
 *
 */
public interface JobTrackingWidget extends IsWidget {

	/**
	 * Start and track a job.
	 * 
	 * @param title The title of the job that is running.
	 * @param isDeterminate True for a determinate job, false for an indeterminate job.
	 * @param type The type of job to run.
	 * @param requestBody The body of the job request.
	 * @param handler Handles the job results.
	 */
	public void startAndTrackJob(String title, boolean isDeterminate, AsynchType type, AsynchronousRequestBody requestBody, AsynchronousProgressHandler handler);
}
