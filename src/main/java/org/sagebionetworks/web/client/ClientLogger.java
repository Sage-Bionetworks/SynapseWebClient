package org.sagebionetworks.web.client;

/**
 * The abstraction of the logger that sends log messages back to the serverlet for logging in the servlet log.
 * @author John
 *
 */
public interface ClientLogger {

	/**
	 * Log an error message
	 * @param message
	 */
	void error(String message);
	
	/**
	 * Send a debug message.
	 * @param message
	 */
	public void debug(String message);
	
	/**
	 * Send an info message
	 * @param message
	 */
	public void info(String message);

}
