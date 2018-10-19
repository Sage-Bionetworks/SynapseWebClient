package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget tracks the progress of an asynchronous job.
 * 
 * @author John
 * 
 */
public class AsynchronousProgressWidget implements
		AsynchronousProgressView.Presenter, JobTrackingWidget {

	/**
	 * The format used to convert doubles to strings.
	 */
	public static final String PERCENT_FORMAT = "0";
	/**
	 * The number of milliseconds to wait between status checks.
	 */
	public static final int WAIT_MS = 500;

	private AsynchronousProgressView view;
	private NumberFormatProvider numberFormatProvider;
	private AsynchronousJobTracker jobTracker;
	private boolean isDeterminate;

	@Inject
	public AsynchronousProgressWidget(AsynchronousProgressView view,
			NumberFormatProvider numberFormatProvider, AsynchronousJobTracker jobTracker) {
		this.view = view;
		this.numberFormatProvider = numberFormatProvider;
		this.numberFormatProvider.setFormat(PERCENT_FORMAT);
		this.jobTracker = jobTracker;
		this.view.setPresenter(this);
	}

	public void setView(AsynchronousProgressView altView) {
		this.view = altView;
		altView.setPresenter(this);
	}
	/**
	 * Reset this widget to track the passed status.
	 * 
	 * @param startMessage
	 * @param statusToTrack
	 */
	@Override
	public void startAndTrackJob(String title, boolean isDeterminate, AsynchType type, AsynchronousRequestBody requestBody,
			final AsynchronousProgressHandler handler) {
		this.isDeterminate = isDeterminate;
		view.setTitle(title);
		view.setIsDetermiante(isDeterminate);
		// Configure this job
		jobTracker.startAndTrack(type, requestBody, WAIT_MS, new UpdatingAsynchProgressHandler() {
					@Override
					public void onFailure(Throwable failure) {
						handler.onFailure(failure);
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

					@Override
					public boolean isAttached() {
						return view.isAttached();
					}
				});
	}

	/**
	 * Set the current progress and update the view.
	 * 
	 * @param status
	 */
	private void setCurrentStatus(AsynchronousJobStatus status) {
		String message = status.getProgressMessage();
		if(isDeterminate){
			double percent = calculateProgressPercent(status);
			String text = numberFormatProvider.format(percent) + "%";
			this.view.setDeterminateProgress(percent, text, message);
		}else{
			this.view.setIndetermianteProgress(message);
		}
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
}
