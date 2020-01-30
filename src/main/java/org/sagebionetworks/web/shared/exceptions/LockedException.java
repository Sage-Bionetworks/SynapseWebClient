package org.sagebionetworks.web.shared.exceptions;

import org.sagebionetworks.repo.model.ErrorResponseCode;

public class LockedException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public LockedException() {
		super();
	}

	public LockedException(String message) {
		super(message);
	}

	public LockedException(String message, ErrorResponseCode code) {
		super(message, code);
	}
}
