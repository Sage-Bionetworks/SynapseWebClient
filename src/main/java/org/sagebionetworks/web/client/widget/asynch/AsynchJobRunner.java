package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Abstraction for the runner that starts a job and gets the results.
 * @author John
 *
 */
public interface AsynchJobRunner {
	
	/**
	 * Start job using the provided request object AsynchronousRequestBody
	 * @param request
	 * @param callback
	 */
	public void startJob(AsynchronousRequestBody request, AsyncCallback<String> callback);
	
	/**
	 * Get the job results for a given jobId.
	 * @param jobId The ID of the job.
	 * @param callback This callback should return the AsynchronousResponseBody
	 */
	public void getJob(String jobId, AsyncCallback<AsynchronousResponseBody> callback);

}
