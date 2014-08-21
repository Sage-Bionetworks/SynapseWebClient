package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchJobState;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider.FireHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * A single use asynchronous job tracker. This is the engine the actually tracks
 * an asynchronous job by checking on its status periodically.
 * 
 * @author jmhill
 * 
 */
public class AsynchronousJobTrackerImpl implements AsynchronousJobTracker {

	private SynapseClientAsync synapseClient;
	private TimerProvider timerProvider;
	private AdapterFactory adapterFactory;
	int waitTimeMS;
	private AsynchronousJobStatus statusToTrack;
	private UpdatingAsynchProgressHandler handler;
	private OneTimeReference<AsynchronousProgressHandler> oneTimeReference;
	private boolean isCanceled;

	@Inject
	public AsynchronousJobTrackerImpl(SynapseClientAsync synapseClient,
			TimerProvider timerProvider, AdapterFactory adapterFactory) {
		super();
		this.synapseClient = synapseClient;
		this.timerProvider = timerProvider;
		this.adapterFactory = adapterFactory;
	}
	
	@Override
	public void configure(AsynchronousJobStatus toTrack, int waitTimeMS,
			UpdatingAsynchProgressHandler handler) {
		this.waitTimeMS = waitTimeMS;
		this.statusToTrack = toTrack;
		this.handler = handler;
		/*
		 * While update can be called many times we only want to call
		 * onComplete(), onFailure() and onCancel() once. For example, it would
		 * be bad to call onSuccess() if we already called onCancel(). This
		 * helps ensure that is the case.
		 */
		this.oneTimeReference = new OneTimeReference<AsynchronousProgressHandler>(
				handler);
		this.isCanceled = false;
	}

	/**
	 * Start tracking this job.
	 */
	public void start() {
		// Setup the timer
		timerProvider.setHandler(new FireHandler() {
			@Override
			public void fire() {
				// when the timer fires the status is checked.
				checkAndWait();
			}
		});
		// There is nothing do to if the job already failed
		if (!AsynchJobState.PROCESSING.equals(statusToTrack.getJobState())) {
			oneTimeOnComplete(statusToTrack);
		} else {
			// Set the current status and start the timer.s
			this.handler.onUpdate(statusToTrack);
			checkAndWait();
		}
	}

	/**
	 * Check the current status and if still processing then wait.
	 * 
	 */
	private void checkAndWait() {
		// Get the current status
		synapseClient.getAsynchJobStatus(this.statusToTrack.getJobId(),
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						if (isCanceled) {
							// Nothing to do if the user has hit cancel.
							return;
						}
						// If the user already canceled
						// Parse the results
						try {
							AsynchronousJobStatus status = new AsynchronousJobStatus(
									adapterFactory.createNew(result));
							// Set the current status
							handler.onUpdate(status);
							// are we done?
							if (!AsynchJobState.PROCESSING.equals(status
									.getJobState())) {
								oneTimeOnComplete(status);
							} else {
								// Start the timer if the user has not canceled
								timerProvider.schedule(waitTimeMS);
							}
						} catch (JSONObjectAdapterException e) {
							oneTimeOnFailure(statusToTrack.getJobId(), e);
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						if (isCanceled) {
							// Nothing to do if the user has hit cancel.
							return;
						}
						oneTimeOnFailure(statusToTrack.getJobId(), caught);
					}
				});
	}

	/**
	 * Cancel tracking this job.
	 */
	public void cancel() {
		isCanceled = true;
		// cancel the timer
		this.timerProvider.cancel();
		oneTimeOnCancel(statusToTrack);
	}

	/**
	 * Will call handler.onCancel() as long as no other handler method, other
	 * than onUpdate() has been called on the handler.
	 * 
	 * @param status
	 */
	private void oneTimeOnCancel(AsynchronousJobStatus status) {
		AsynchronousProgressHandler mightBeNull = this.oneTimeReference
				.getReference();
		if (mightBeNull != null) {
			mightBeNull.onCancel(status);
		}
	}

	/**
	 * Will call handler.onComplete() as long as no other handler method, other
	 * than onUpdate() has been called on the handler.
	 * 
	 * @param status
	 */
	private void oneTimeOnComplete(AsynchronousJobStatus status) {
		AsynchronousProgressHandler mightBeNull = this.oneTimeReference
				.getReference();
		if (mightBeNull != null) {
			mightBeNull.onComplete(status);
		}
	}

	/**
	 * Will call handler.onFailure() as long as no other handler method, other
	 * than onUpdate() has been called on the handler.
	 * 
	 * @param jobId
	 * @param caught
	 */
	private void oneTimeOnFailure(String jobId, Throwable caught) {
		AsynchronousProgressHandler mightBeNull = this.oneTimeReference
				.getReference();
		if (mightBeNull != null) {
			mightBeNull.onStatusCheckFailure(jobId, caught);
		}
	}

}
