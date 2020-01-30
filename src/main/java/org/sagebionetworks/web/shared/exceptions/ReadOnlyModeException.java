package org.sagebionetworks.web.shared.exceptions;

public class ReadOnlyModeException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public ReadOnlyModeException() {
		super();
	}

	public ReadOnlyModeException(String message) {
		super(message);
	}

}
