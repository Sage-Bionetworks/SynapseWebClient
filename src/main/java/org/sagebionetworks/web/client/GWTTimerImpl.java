package org.sagebionetworks.web.client;

import com.google.gwt.user.client.Timer;

public class GWTTimerImpl implements GWTTimer {

	Timer timer;

	@Override
	public void cancel() {
		if (timer != null) {
			timer.cancel();
		}
	}

	@Override
	public void schedule(int delayMillis) {
		if (timer != null) {
			timer.schedule(delayMillis);
		}
	}

	@Override
	public void configure(final Runnable runnable) {
		timer = new Timer() {
			@Override
			public void run() {
				runnable.run();
			}
		};
	}

}
