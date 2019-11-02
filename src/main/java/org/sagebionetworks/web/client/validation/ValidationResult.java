package org.sagebionetworks.web.client.validation;

public class ValidationResult {

	public final static String IS_REQUIRED = " is required. ";
	boolean valid = true;
	String errorMessage = "";

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

	public ValidationResult requiredField(String fieldName, String fieldValue) {
		if (fieldValue == null || fieldValue.equals("")) {
			valid = false;
			errorMessage += fieldName + IS_REQUIRED;
		}
		return this;
	}
}
