package org.sagebionetworks.web.shared.exceptions;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;

/**
 * This exception is used to communicate that a job is not ready.
 * The status is AsynchronousJobStatus object.
 * @author John
 *
 */
public class ResultNotReadyException extends RestServiceException  {

	private static final long serialVersionUID = 1L;
	private AsynchronousJobStatus status;

	public ResultNotReadyException() {
		super();
	}

	/**
	 * 
	 * @param statusJson JSON serialized AsynchronousJobStatus object 
	 */
	public ResultNotReadyException(AsynchronousJobStatus status) {
		super(status.getProgressMessage());
		this.status = status;
	}

	/**
	 * 
	 * @return AsynchronousJobStatus json model string
	 */
	public AsynchronousJobStatus getStatus() {
		return status;
	}
}
