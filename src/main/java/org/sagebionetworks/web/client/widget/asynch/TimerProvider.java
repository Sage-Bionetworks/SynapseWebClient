package org.sagebionetworks.web.client.widget.asynch;

/**
 * An abstraction for a timer.
 * 
 * @author John
 *
 */
public interface TimerProvider {

	/**
	 * The handler will is called when the timer fires.
	 * 
	 * @param handler
	 */
	public void setHandler(FireHandler handler);

	/**
	 * Setup a single wait.
	 * 
	 * @param delayMillis
	 */
	public void schedule(int delayMillis);

	/**
	 * Cancel the timer if it is running.
	 */
	public void cancel();

	/**
	 * Handler for timer fire events.
	 *
	 */
	public interface FireHandler {

		/**
		 * Called when the timer fires.
		 */
		public void fire();
	}

}
