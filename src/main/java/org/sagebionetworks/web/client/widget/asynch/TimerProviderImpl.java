package org.sagebionetworks.web.client.widget.asynch;


import com.google.gwt.user.client.Timer;

/**
 * Basic wrapper for a GWT timer.
 * 
 * @author John
 *
 */
public class TimerProviderImpl implements TimerProvider {

	Timer timer;

	/**
	 * This method must be called before the timer can be used.
	 */
	@Override
	public void setHandler(final FireHandler handler) {
		this.timer = new Timer() {
			@Override
			public void run() {
				handler.fire();
			}
		};
	}

	@Override
	public void schedule(int delayMillis) {
		validate();
		timer.schedule(delayMillis);
	}

	/**
	 * Is the timer ready?
	 */
	private void validate() {
		if (timer == null) {
			throw new IllegalArgumentException("FireHandler must before this timer can be used.");
		}
	}

	@Override
	public void cancel() {
		validate();
		timer.cancel();
	}

}
