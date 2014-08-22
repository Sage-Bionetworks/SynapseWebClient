package org.sagebionetworks.web.unitclient.widget.asynch;

import java.util.List;

import org.sagebionetworks.repo.model.asynch.AsynchJobState;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;

/**
 * Stub to simulate a job tracker.
 * @author John
 *
 */
public class AsynchronousJobTrackerStub implements AsynchronousJobTracker {

	List<AsynchronousJobStatus> states;
	int waitTimeMS;
	UpdatingAsynchProgressHandler handler;
	Throwable error;
	
	public AsynchronousJobTrackerStub(List<AsynchronousJobStatus> states, Throwable error){
		this.states = states;
		this.error = error;
	}

	@Override
	public void cancel() {
		// Simulate a cancel
		handler.onCancel(this.states.get(0));
	}

	@Override
	public void startAndTrack(AsynchronousRequestBody requestBody,
			int waitTimeMS, UpdatingAsynchProgressHandler handler) {
		this.waitTimeMS = waitTimeMS;
		this.handler = handler;
		if(error != null){
			handler.onStatusCheckFailure(error);
		}else{
			// cycle through the states.
			for(AsynchronousJobStatus state: states){
				handler.onUpdate(state);
				if(!AsynchJobState.PROCESSING.equals(state.getJobState())){
					handler.onComplete(state);
					break;
				}
			}
		}
	}

}
