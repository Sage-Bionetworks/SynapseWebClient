package org.sagebionetworks.web.client.widget.discussion.modal;

public class ValidationResult {

	boolean valid;
	String errorMessage;

	public boolean isValid() {
		return valid;
	}
	public void setValidity(boolean result) {
		this.valid = result;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
