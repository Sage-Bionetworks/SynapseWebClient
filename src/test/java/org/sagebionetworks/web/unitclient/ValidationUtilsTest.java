package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.sagebionetworks.web.client.ValidationUtils;

public class ValidationUtilsTest {

	@Test
	public void testValidWidgetName() {
		assertTrue(ValidationUtils.isValidWidgetName("a widget name"));
		assertTrue(ValidationUtils.isValidWidgetName("special characters allowed (-+) and 01239"));

		assertFalse(ValidationUtils.isValidWidgetName("special characters disallowed like *$"));
		assertFalse(ValidationUtils.isValidWidgetName(null));
		assertFalse(ValidationUtils.isValidWidgetName(""));
	}

	@Test
	public void testIsValidUrl() {
		assertTrue(ValidationUtils.isValidUrl("https://www.youtube.com/watch?v=m86ae_e_ptU", false));
		assertTrue(ValidationUtils.isValidUrl("http://www.google.com", false));
		assertTrue(ValidationUtils.isValidUrl("#!Synapse:syn123", false));

		assertFalse(ValidationUtils.isValidUrl("http:/www.google.com", false));
		assertFalse(ValidationUtils.isValidUrl("missingprotocol.com", false));

		// undefined url handling
		assertTrue(ValidationUtils.isValidUrl("", true));
		assertFalse(ValidationUtils.isValidUrl("", false));

		assertTrue(ValidationUtils.isValidUrl(null, true));
		assertFalse(ValidationUtils.isValidUrl(null, false));
	}

}
