package org.sagebionetworks.web.shared.exceptions;

/**
 * Thrown when there is a table parse exception.
 * 
 * @author John
 *
 */
public class TableQueryParseException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	public TableQueryParseException() {
		super();
	}

	public TableQueryParseException(String message) {
		super(message);
	}
}
