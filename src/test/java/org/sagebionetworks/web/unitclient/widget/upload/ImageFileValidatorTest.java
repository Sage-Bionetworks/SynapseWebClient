package org.sagebionetworks.web.unitclient.widget.upload;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.upload.ImageFileValidator;


public class ImageFileValidatorTest {

	ImageFileValidator imageValidator;
	
	@Before
	public void setup() {
		imageValidator = new ImageFileValidator();
	}
	@Test
	public void testImage() {
		assertTrue(imageValidator.isValid("test.jpg"));
		assertTrue(imageValidator.isValid("test.jpeg"));
		assertTrue(imageValidator.isValid("test.png"));
		assertTrue(imageValidator.isValid("test.bmp"));
		assertTrue(imageValidator.isValid("test.gif"));
		assertTrue(imageValidator.isValid("test.jpe"));
		assertTrue(imageValidator.isValid("test.pjpeg"));
	}
	
	@Test
	public void testNonImage() {
		assertFalse(imageValidator.isValid("test.txt"));
		assertFalse(imageValidator.isValid("test.pdf"));
		assertFalse(imageValidator.isValid("test.csv"));
		assertFalse(imageValidator.isValid("test"));
	}
	
	
}
