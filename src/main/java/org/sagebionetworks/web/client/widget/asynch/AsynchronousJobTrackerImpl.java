package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider.FireHandler;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.ResultNotReadyException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * A single use asynchronous job tracker. This is the engine the actually tracks an asynchronous job
 * by checking on its status periodically.
 * 
 * @author jmhill
 * 
 */
public class AsynchronousJobTrackerImpl implements AsynchronousJobTracker {
	private TimerProvider timerProvider;
	private int waitTimeMS;
	private AsynchType type;
	private String jobId;
	private UpdatingAsynchProgressHandler handler;
	private OneTimeReference<AsynchronousProgressHandler> oneTimeReference;
	private boolean isCanceled;
	private SynapseJavascriptClient jsClient;

	@Inject
	public AsynchronousJobTrackerImpl(TimerProvider timerProvider, SynapseJavascriptClient jsClient) {
		super();
		this.timerProvider = timerProvider;
		this.jsClient = jsClient;
	}

	/**
	 * Start the job then start tracking the job
	 */
	public void startAndTrack(AsynchType type, final AsynchronousRequestBody requestBody, final int waitTimeMS, final UpdatingAsynchProgressHandler handler) {
		this.isCanceled = false;
		this.handler = handler;
		this.type = type;
		/*
		 * While update can be called many times we only want to call onComplete(), onFailure() and
		 * onCancel() once. For example, it would be bad to call onSuccess() if we already called
		 * onCancel(). This helps ensure that is the case.
		 */
		this.oneTimeReference = new OneTimeReference<AsynchronousProgressHandler>(handler);

		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String jobId) {
				// nothing to do if canceled.
				if (!isCanceled) {
					// Track the job.
					trackJob(jobId, requestBody, waitTimeMS);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				if (!isCanceled) {
					oneTimeOnFailure(caught);
				}
			}
		};
		// Start the job.
		jsClient.startAsynchJob(type, requestBody, callback);
	}

	/**
	 * Once the job has started
	 * 
	 * @param statusToTrack
	 * @param waitTimeMS
	 * @param handler
	 */
	private void trackJob(String jobId, final AsynchronousRequestBody requestBody, int waitTimeMS) {
		this.waitTimeMS = waitTimeMS;
		this.jobId = jobId;
		// Setup the timer
		timerProvider.setHandler(new FireHandler() {
			@Override
			public void fire() {
				// Only continue to fire if the handler is still attached to the UI,
				if (handler.isAttached()) {
					// when the timer fires the status is checked.
					checkAndWait(requestBody);
				}
			}
		});
		// Do the first check and wait.
		checkAndWait(requestBody);
	}

	/**
	 * Check the current status and if still processing then wait.
	 * 
	 */
	private void checkAndWait(AsynchronousRequestBody requestBody) {
		// Get the current status
		jsClient.getAsynchJobResults(this.type, this.jobId, requestBody, new AsyncCallback<AsynchronousResponseBody>() {
			@Override
			public void onSuccess(AsynchronousResponseBody response) {
				// nothing to do if canceled.
				if (!isCanceled) {
					oneTimeOnComplete(response);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				// nothing to do if canceled.
				if (!isCanceled) {
					// When the job is not
					if (caught instanceof ResultNotReadyException) {
						ResultNotReadyException rnre = (ResultNotReadyException) caught;
						// Extract the status
						AsynchronousJobStatus status = rnre.getStatus();
						handler.onUpdate(status);
						// start the timer and wait for another push
						timerProvider.schedule(waitTimeMS);
					} else {
						// Failed.
						oneTimeOnFailure(caught);
					}
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
		oneTimeOnCancel();
	}

	/**
	 * Will call handler.onCancel() as long as no other handler method, other than onUpdate() has been
	 * called on the handler.
	 * 
	 * @param status
	 */
	private void oneTimeOnCancel() {
		AsynchronousProgressHandler mightBeNull = this.oneTimeReference.getReference();
		if (mightBeNull != null) {
			mightBeNull.onCancel();
		}
	}

	/**
	 * Will call handler.onComplete() as long as no other handler method, other than onUpdate() has been
	 * called on the handler.
	 * 
	 * @param status
	 */
	private void oneTimeOnComplete(AsynchronousResponseBody response) {
		AsynchronousProgressHandler mightBeNull = this.oneTimeReference.getReference();
		if (mightBeNull != null) {
			mightBeNull.onComplete(response);
		}
	}

	/**
	 * Will call handler.onFailure() as long as no other handler method, other than onUpdate() has been
	 * called on the handler.
	 * 
	 * @param jobId
	 * @param caught
	 */
	private void oneTimeOnFailure(Throwable caught) {
		AsynchronousProgressHandler mightBeNull = this.oneTimeReference.getReference();
		if (mightBeNull != null) {
			mightBeNull.onFailure(caught);
		}
	}
}
