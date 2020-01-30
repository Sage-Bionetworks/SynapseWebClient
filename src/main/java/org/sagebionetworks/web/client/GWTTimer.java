package org.sagebionetworks.web.client;

public interface GWTTimer {
	void cancel();

	void schedule(int delayMillis);

	void configure(Runnable runnable);
}
