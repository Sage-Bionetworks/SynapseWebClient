package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchJobState;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
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

	/**
	 * Start the job then start tracking the job
	 */
	public void startAndTrack(AsynchronousRequestBody requestBody,
			final int waitTimeMS, final UpdatingAsynchProgressHandler handler) {
		this.isCanceled = false;
		this.handler = handler;
		/*
		 * While update can be called many times we only want to call
		 * onComplete(), onFailure() and onCancel() once. For example, it would
		 * be bad to call onSuccess() if we already called onCancel(). This
		 * helps ensure that is the case.
		 */
		this.oneTimeReference = new OneTimeReference<AsynchronousProgressHandler>(
				handler);
		// Start the job.
		JSONObjectAdapter adapter = adapterFactory.createNew();
		try {
			requestBody.writeToJSONObject(adapter);
			String jobBodyJSON = adapter.toJSONString();
			synapseClient.startAsynchJob(jobBodyJSON,
					new AsyncCallback<String>() {
						@Override
						public void onSuccess(String statusJSON) {
							// nothing to do if canceled.
							if (!isCanceled) {
								try {
									AsynchronousJobStatus status = new AsynchronousJobStatus(
											adapterFactory
													.createNew(statusJSON));
									// Track the job.
									trackJob(status, waitTimeMS);
								} catch (JSONObjectAdapterException e) {
									oneTimeOnFailure(e);
								}
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							if (!isCanceled) {
								oneTimeOnFailure(caught);
							}
						}
					});
		} catch (JSONObjectAdapterException e) {
			if (!isCanceled) {
				oneTimeOnFailure(e);
			}
		}
	}

	/**
	 * Once the job has started
	 * 
	 * @param statusToTrack
	 * @param waitTimeMS
	 * @param handler
	 */
	private void trackJob(AsynchronousJobStatus statusToTrack, int waitTimeMS) {
		this.waitTimeMS = waitTimeMS;
		this.statusToTrack = statusToTrack;
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
						// nothing to do if canceled.
						if (!isCanceled) {
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
								oneTimeOnFailure(e);
							}
						}

					}

					@Override
					public void onFailure(Throwable caught) {
						// nothing to do if canceled.
						if (!isCanceled) {
							oneTimeOnFailure(caught);
						}

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
	private void oneTimeOnFailure(Throwable caught) {
		AsynchronousProgressHandler mightBeNull = this.oneTimeReference
				.getReference();
		if (mightBeNull != null) {
			mightBeNull.onStatusCheckFailure(caught);
		}
	}
}
