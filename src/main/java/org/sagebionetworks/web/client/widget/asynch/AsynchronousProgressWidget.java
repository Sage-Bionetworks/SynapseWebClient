package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.SynapseClientAsync;

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
	private NumberFormatProvider numberFormatProvider;
	private TimerProvider timerProvider;;
	private AdapterFactory adapterFactory;
	private String trackingJobId;
	private AsynchronousJobTracker jobTracker;

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
			final AsynchronousProgressHandler handler) {
		view.setTitle(title);
		// This is the jobId we are currently tracking
		// Tracking a new job will "disconnect" this widget from any previously
		// running trackers.
		trackingJobId = statusToTrack.getJobId();
		// We create a new job tacker for each job.
		jobTracker = new AsynchronousJobTracker(synapseClient, timerProvider,
				adapterFactory, WAIT_MS, statusToTrack,
				new UpdatingAsynchProgressHandler() {
					@Override
					public void onStatusCheckFailure(String jobId, Throwable failure) {
						// We only care about the current job (not old jobs)
						if (trackingJobId.equals(jobId)) {
							handler.onStatusCheckFailure(trackingJobId, failure);
						}
					}

					@Override
					public void onComplete(AsynchronousJobStatus status) {
						if (trackingJobId.equals(status.getJobId())) {
							handler.onComplete(status);
						}
					}

					@Override
					public void onCancel(AsynchronousJobStatus status) {
						if (trackingJobId.equals(status.getJobId())) {
							handler.onCancel(status);
						}
					}

					@Override
					public void onUpdate(AsynchronousJobStatus status) {
						if (trackingJobId.equals(status.getJobId())) {
							setCurrentStatus(status);
						}
					}
				});
		// Start the tracker
		jobTracker.start();
	}

	/**
	 * Set the current progress and update the view.
	 * 
	 * @param status
	 */
	private void setCurrentStatus(AsynchronousJobStatus status) {
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
		// Calling cancel on the tracker will feed-back to this widget.
		jobTracker.cancel();
	}

}
