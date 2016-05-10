package org.sagebionetworks.web.shared.exceptions;

public class LockedException extends RestServiceException {
	
	private static final long serialVersionUID = 1L;

	public LockedException() {
		super();
	}

	public LockedException(String message) {
		super(message);
	}

}
