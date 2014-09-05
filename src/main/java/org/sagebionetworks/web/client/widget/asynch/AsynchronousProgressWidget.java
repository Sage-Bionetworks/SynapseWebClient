package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;

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
	private NumberFormatProvider numberFormatProvider;
	private AsynchronousJobTracker jobTracker;

	@Inject
	public AsynchronousProgressWidget(AsynchronousProgressView view,
			NumberFormatProvider numberFormatProvider, AsynchronousJobTracker jobTracker) {
		this.view = view;
		this.numberFormatProvider = numberFormatProvider;
		this.numberFormatProvider.setFormat(PERCENT_FORMAT);
		this.jobTracker = jobTracker;
		this.view.setPresenter(this);
	}

	/**
	 * Reset this widget to track the passed status.
	 * 
	 * @param startMessage
	 * @param statusToTrack
	 */
	public void configure(String title,AsynchJobRunner runner, AsynchronousRequestBody requestBody,
			final AsynchronousProgressHandler handler) {
		view.setTitle(title);
		// Configure this job
		jobTracker.startAndTrack(runner, requestBody, WAIT_MS, new UpdatingAsynchProgressHandler() {
					@Override
					public void onStatusCheckFailure(Throwable failure) {
						handler.onStatusCheckFailure(failure);
					}

					@Override
					public void onCancel() {
						handler.onCancel();
					}

					@Override
					public void onUpdate(AsynchronousJobStatus status) {
						setCurrentStatus(status);
					}

					@Override
					public void onComplete(AsynchronousResponseBody response) {
						handler.onComplete(response);
					}
				});
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
				|| status.getProgressTotal() == null || status.getProgressTotal() < 1l) {
			return 0.0;
		}
		double current = status.getProgressCurrent();
		double total = status.getProgressTotal();
		return current / total * 100.0;
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
