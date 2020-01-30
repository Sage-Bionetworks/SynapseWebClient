package org.sagebionetworks.web.client.exceptions;

public class DuplicateKeyException extends Exception {
	private static final long serialVersionUID = -5425239249145700239L;

	public DuplicateKeyException(String message) {
		super(message);
	}

	public DuplicateKeyException() {}
}
