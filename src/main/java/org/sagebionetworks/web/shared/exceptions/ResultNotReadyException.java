package org.sagebionetworks.web.shared.exceptions;

/**
 * This exception is used to communicate that a job is not ready.
 * The status the JSON for an AsynchronousJobStatus object.
 * @author John
 *
 */
public class ResultNotReadyException extends RestServiceException  {

	private static final long serialVersionUID = 1L;
	private String statusJson;

	public ResultNotReadyException() {
		super();
	}

	/**
	 * 
	 * @param statusJson JSON serialized AsynchronousJobStatus object 
	 */
	public ResultNotReadyException(String statusJson) {
		super(statusJson);
		this.statusJson = statusJson;
	}

	/**
	 * 
	 * @return AsynchronousJobStatus json model string
	 */
	public String getStatusJson() {
		return statusJson;
	}
}
