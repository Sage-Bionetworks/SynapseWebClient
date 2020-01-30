package org.sagebionetworks.web.unitclient.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sagebionetworks.web.client.validation.ValidationResult.IS_REQUIRED;
import org.junit.Test;
import org.sagebionetworks.web.client.validation.ValidationResult;

public class ValidationResultTest {

	@Test
	public void testOneValidArgument() {
		ValidationResult result = new ValidationResult();
		result.requiredField("title", "title");
		assertTrue(result.isValid());
	}

	@Test
	public void testOneNullArguments() {
		ValidationResult result = new ValidationResult();
		result.requiredField("title", null);
		assertFalse(result.isValid());
		assertTrue(result.getErrorMessage().contains("title"));
		assertTrue(result.getErrorMessage().contains(IS_REQUIRED));
	}

	@Test
	public void testOneEmptyArguments() {
		ValidationResult result = new ValidationResult();
		result.requiredField("title", "");
		assertFalse(result.isValid());
		assertTrue(result.getErrorMessage().contains("title"));
		assertTrue(result.getErrorMessage().contains(IS_REQUIRED));
	}

	@Test
	public void testValidInvalidArguments() {
		ValidationResult result = new ValidationResult();
		result.requiredField("title", "title").requiredField("message", null);
		assertFalse(result.isValid());
		assertTrue(result.getErrorMessage().contains("message"));
		assertTrue(result.getErrorMessage().contains(IS_REQUIRED));
	}

	@Test
	public void testInvalidValidArguments() {
		ValidationResult result = new ValidationResult();
		result.requiredField("title", "").requiredField("message", "message");
		assertFalse(result.isValid());
		assertTrue(result.getErrorMessage().contains("title"));
		assertTrue(result.getErrorMessage().contains(IS_REQUIRED));
	}

	@Test
	public void testInvalidArguments() {
		ValidationResult result = new ValidationResult();
		result.requiredField("title", "").requiredField("message", "");
		assertFalse(result.isValid());
		assertTrue(result.getErrorMessage().contains("title"));
		assertTrue(result.getErrorMessage().contains("message"));
		assertTrue(result.getErrorMessage().contains(IS_REQUIRED));
	}

	@Test
	public void testValidArguments() {
		ValidationResult result = new ValidationResult();
		result.requiredField("title", "title").requiredField("message", "message");
		assertTrue(result.isValid());
	}
}
