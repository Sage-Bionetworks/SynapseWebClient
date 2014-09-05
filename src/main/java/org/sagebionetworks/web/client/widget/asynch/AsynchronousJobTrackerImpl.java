package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider.FireHandler;
import org.sagebionetworks.web.shared.exceptions.ResultNotReadyException;

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

	private TimerProvider timerProvider;
	private AdapterFactory adapterFactory;
	AsynchJobRunner runner;
	private UpdatingAsynchProgressHandler handler;
	private OneTimeReference<AsynchronousProgressHandler> oneTimeHandler;
	int waitTimeMS;
	private String jobId;
	private boolean isCanceled;

	@Inject
	public AsynchronousJobTrackerImpl(TimerProvider timerProvider, AdapterFactory adapterFactory) {
		super();
		this.timerProvider = timerProvider;
		this.adapterFactory = adapterFactory;
	}

	/**
	 * Start the job then start tracking the job
	 */
	public void startAndTrack(final AsynchJobRunner runner, AsynchronousRequestBody request,
			final int waitTimeMS, final UpdatingAsynchProgressHandler handler) {
		this.runner = runner;
		this.isCanceled = false;
		this.handler = handler;
		/*
		 * While update can be called many times we only want to call
		 * onComplete(), onFailure() and onCancel() once. For example, it would
		 * be bad to call onSuccess() if we already called onCancel(). This
		 * helps ensure that is the case.
		 */
		oneTimeHandler = new OneTimeReference<AsynchronousProgressHandler>(handler);
		// Start the job.
		runner.startJob(request, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String jobId) {
				// nothing to do if canceled.
				if (!isCanceled) {
					// Track the job.
					trackJob(jobId, waitTimeMS);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				if (!isCanceled) {
					oneTimeOnFailure(caught);
				}
			}
		});
	}

	/**
	 * Once the job has started
	 * 
	 * @param statusToTrack
	 * @param waitTimeMS
	 * @param handler
	 */
	private void trackJob(String jobId, int waitTimeMS) {
		this.waitTimeMS = waitTimeMS;
		this.jobId = jobId;
		// Setup the timer
		timerProvider.setHandler(new FireHandler() {
			@Override
			public void fire() {
				// when the timer fires the status is checked.
				checkAndWait();
			}
		});
		// There is nothing do to if the job already failed
		checkAndWait();
	}

	/**
	 * Check the current status and if still processing then wait.
	 * 
	 */
	private void checkAndWait() {
		// Get the current status
		runner.getJob(this.jobId, new AsyncCallback<AsynchronousResponseBody>() {

			@Override
			public void onFailure(Throwable caught) {
				// This can happen if there is a real error or if the job is not ready yet.
				if(!isCanceled){
					if(caught instanceof ResultNotReadyException){
						ResultNotReadyException rnre = (ResultNotReadyException) caught;
						try {
							AsynchronousJobStatus status = new AsynchronousJobStatus(adapterFactory.createNew(rnre.getStatusJson()));
							handler.onUpdate(status);
							// Start the timer if the user has not canceled
							timerProvider.schedule(waitTimeMS);
						} catch (JSONObjectAdapterException e) {
							oneTimeOnFailure(e);
						}
					}else{
						// This is a real failure
						oneTimeOnFailure(caught);
					}
				}
			}

			@Override
			public void onSuccess(AsynchronousResponseBody result) {
				if(!isCanceled){
					oneTimeOnComplete(result);
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
	 * Will call handler.onCancel() as long as no other handler method, other
	 * than onUpdate() has been called on the handler.
	 * 
	 * @param status
	 */
	private void oneTimeOnCancel() {
		AsynchronousProgressHandler mightBeNull = oneTimeHandler.getReference();
		if (mightBeNull != null) {
			mightBeNull.onCancel();
		}
	}

	/**
	 * Will call handler.onComplete() as long as no other handler method, other
	 * than onUpdate() has been called on the handler.
	 * 
	 * @param status
	 */
	private void oneTimeOnComplete(AsynchronousResponseBody results) {
		AsynchronousProgressHandler mightBeNull = oneTimeHandler.getReference();
		if (mightBeNull != null) {
			mightBeNull.onComplete(results);
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
		AsynchronousProgressHandler mightBeNull = oneTimeHandler.getReference();
		if (mightBeNull != null) {
			mightBeNull.onStatusCheckFailure(caught);
		}
	}
}
