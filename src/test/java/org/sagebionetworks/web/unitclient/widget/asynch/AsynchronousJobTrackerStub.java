package org.sagebionetworks.web.unitclient.widget.asynch;

import java.util.List;
import org.sagebionetworks.repo.model.asynch.AsynchJobState;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.shared.asynch.AsynchType;

/**
 * Stub to simulate a job tracker.
 * 
 * @author John
 *
 */
public class AsynchronousJobTrackerStub implements AsynchronousJobTracker {

	List<AsynchronousJobStatus> states;
	int waitTimeMS;
	UpdatingAsynchProgressHandler handler;
	Throwable error;
	AsynchronousResponseBody response;

	public AsynchronousJobTrackerStub(List<AsynchronousJobStatus> states, Throwable error, AsynchronousResponseBody response) {
		this.states = states;
		this.error = error;
		this.response = response;
	}

	@Override
	public void cancel() {
		// Simulate a cancel
		handler.onCancel();
	}

	@Override
	public void startAndTrack(AsynchType type, AsynchronousRequestBody requestBody, int waitTimeMS, UpdatingAsynchProgressHandler handler) {
		this.waitTimeMS = waitTimeMS;
		this.handler = handler;
		if (error != null) {
			handler.onFailure(error);
		} else {
			// cycle through the states.
			for (AsynchronousJobStatus state : states) {
				handler.onUpdate(state);
				if (!AsynchJobState.PROCESSING.equals(state.getJobState())) {
					handler.onComplete(this.response);
					break;
				}
			}
		}
	}

}
