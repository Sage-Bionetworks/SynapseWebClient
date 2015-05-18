package org.sagebionetworks.web.unitclient.widget.upload;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.upload.ImageFileValidator;
import org.sagebionetworks.web.client.widget.upload.TableFileValidator;

public class TableFileValidatorTest {

	TableFileValidator tableValidator;
	
	@Before
	public void setup() {
		tableValidator = new TableFileValidator();
	}
	@Test
	public void testTable() {
		assertTrue(tableValidator.isValid("test.csv"));
		assertTrue(tableValidator.isValid("test.tab-separated-values"));
		assertTrue(tableValidator.isValid("test.txt"));
		assertTrue(tableValidator.isValid("test.tsv"));
	}
	
	@Test
	public void testNotTable() {
		assertFalse(tableValidator.isValid("test.gif"));
		assertFalse(tableValidator.isValid("test.jpe"));
		assertFalse(tableValidator.isValid("test.pjpeg"));
		assertFalse(tableValidator.isValid("test.pdf"));
		assertFalse(tableValidator.isValid("test"));
	}
}
