package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchJobState;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider.FireHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget tracks the progress of an asynchronous job.
 * 
 * @author John
 * 
 */
public class AsynchronousProgressWidget implements
		AsynchronousProgressView.Presenter, IsWidget {

	/**
	 * The format used to convert doubles to strings.
	 */
	public static final String PERCENT_FORMAT = "000.00";
	/**
	 * The number of milliseconds to wait between status checks.
	 */
	public static final int WAIT_MS = 1000;

	private AsynchronousProgressView view;
	private SynapseClientAsync synapseClient;
	private OneTimeReference<AsynchronousProgressHandler> handlerReference;
	private NumberFormatProvider numberFormatProvider;
	private TimerProvider timerProvider;
	private AsynchronousJobStatus currentStatus;
	private AdapterFactory adapterFactory;
	private boolean isCanceled;

	@Inject
	public AsynchronousProgressWidget(AsynchronousProgressView view,
			SynapseClientAsync synapseClient,
			NumberFormatProvider numberFormatProvider,
			TimerProvider timerProvider, AdapterFactory adapterFactory) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.numberFormatProvider = numberFormatProvider;
		this.numberFormatProvider.setFormat(PERCENT_FORMAT);
		this.timerProvider = timerProvider;
		this.adapterFactory = adapterFactory;
		this.view.setPresenter(this);
	}

	/**
	 * Reset this widget to track the passed status.
	 * 
	 * @param startMessage
	 * @param statusToTrack
	 */
	public void configure(String title, AsynchronousJobStatus statusToTrack,
			AsynchronousProgressHandler handler) {
		view.setTitle(title);
		isCanceled = false;
		/*
		 * OneTimeReference ensure we only call a single method of the handler
		 * even under conditions where asynchronous calls return after a user
		 * clicks cancel.
		 */
		this.handlerReference = new OneTimeReference<AsynchronousProgressHandler>(
				handler);
		this.currentStatus = statusToTrack;
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
			handler.onComplete(statusToTrack);
		} else {
			// Set the current status and start the timer.s
			setCurrentStatus(statusToTrack);
			checkAndWait();
		}
	}

	/**
	 * Check the current status and if still processing then wait.
	 * 
	 */
	private void checkAndWait() {
		// Get the current status
		synapseClient.getAsynchJobStatus(this.currentStatus.getJobId(),
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						// If the user already canceled
						// Parse the results
						try {
							AsynchronousJobStatus status = new AsynchronousJobStatus(
									adapterFactory.createNew(result));
							// Set the current status
							setCurrentStatus(status);
							// are we done?
							if (!AsynchJobState.PROCESSING.equals(status
									.getJobState())) {
								attemptCallOnConplete(status);
							} else {
								// Start the timer if the user has not canceled
								if (!isCanceled) {
									// Setup another wait
									timerProvider.schedule(WAIT_MS);
								}
							}
						} catch (JSONObjectAdapterException e) {
							attemptCallOnFailure(e);
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						attemptCallOnFailure(caught);
					}
				});
	}

	/**
	 * Attempt to notify the handler of completion. Only the first call to a
	 * handler will succeed. All subsequent calls will be swallowed. For
	 * example, it would be bad to call onCancel() after calling onComplete().
	 * 
	 * @param status
	 */
	private void attemptCallOnConplete(AsynchronousJobStatus status) {
		AsynchronousProgressHandler handler = handlerReference.getReference();
		// if the handler is null then we have already called one of the handler
		// methods and must not call another.
		if (handler != null) {
			handler.onComplete(status);
		}
	}

	/**
	 * Attempt to notify the handler of the failure. Only the first call to a
	 * handler will succeed. All subsequent calls will be swallowed. For
	 * example, it would be bad to call onCancel() after calling onComplete().
	 * 
	 * @param caught
	 */
	private void attemptCallOnFailure(Throwable caught) {
		AsynchronousProgressHandler handler = handlerReference.getReference();
		// if the handler is null then we have already called one of the handler
		// methods and must not call another.
		if (handler != null) {
			handler.onFailure(caught);
		}
	}

	/**
	 * Attempt to notify the handler of a cancel. Only the first call to a
	 * handler will succeed. All subsequent calls will be swallowed. For
	 * example, it would be bad to call onCancel() after calling onComplete().
	 * 
	 * @param caught
	 */
	private void attemptCallOnCancel() {
		AsynchronousProgressHandler handler = handlerReference.getReference();
		// if the handler is null then we have already called one of the handler
		// methods and must not call another.
		if (handler != null) {
			handler.onCancel(this.currentStatus);
		}
	}

	/**
	 * Set the current progress and update the view.
	 * 
	 * @param status
	 */
	private void setCurrentStatus(AsynchronousJobStatus status) {
		this.currentStatus = status;
		double percent = calculateProgressPercent(status);
		String text = numberFormatProvider.format(percent) + "%";
		this.view.setProgress(percent, text, status.getProgressMessage());
	}

	/**
	 * Calculate the progress
	 * 
	 * @param status
	 * @return
	 */
	private double calculateProgressPercent(AsynchronousJobStatus status) {
		if (status.getProgressCurrent() == null
				|| status.getProgressTotal() == null) {
			return 0.0;
		}
		double current = status.getProgressCurrent();
		double total = status.getProgressTotal();
		return current / total;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onCancel() {
		isCanceled = true;
		// cancel the timer
		this.timerProvider.cancel();
		attemptCallOnCancel();
	}

}
