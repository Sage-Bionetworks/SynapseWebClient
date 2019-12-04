package org.sagebionetworks.web.unitclient.widget.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.ImageFileValidator;
import org.sagebionetworks.web.shared.WebConstants;


public class ImageFileValidatorTest {

	ImageFileValidator imageValidator;
	FileMetadata mockMeta;

	@Before
	public void setup() {
		imageValidator = new ImageFileValidator();
		mockMeta = mock(FileMetadata.class);
	}

	@Test
	public void testImage() {
		when(mockMeta.getContentType()).thenReturn("image/jpg");
		when(mockMeta.getFileName()).thenReturn("test.jpg");
		when(mockMeta.getFileSize()).thenReturn(10.0);
		assertTrue(imageValidator.getMaxFileSize() == null);
		assertTrue(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertTrue(imageValidator.isValid(mockMeta));


		when(mockMeta.getContentType()).thenReturn("image/png");
		when(mockMeta.getFileName()).thenReturn("test.png");
		assertTrue(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertTrue(imageValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("image/gif");
		when(mockMeta.getFileName()).thenReturn("test.gif");
		assertTrue(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertTrue(imageValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("image/jpe");
		when(mockMeta.getFileName()).thenReturn("test.jpe");
		assertTrue(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertTrue(imageValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("image/pjpeg");
		when(mockMeta.getFileName()).thenReturn("test.pjpeg");
		assertTrue(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertTrue(imageValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("image/svg+xml");
		when(mockMeta.getFileName()).thenReturn("test.svg");
		assertTrue(imageValidator.isValid(mockMeta));

		// test non-null max file size
		imageValidator.setMaxSize(100.0);
		assertTrue(imageValidator.isValid(mockMeta));
		// unit test isValidSize
		assertTrue(imageValidator.isValidSize(10.0));
		assertFalse(imageValidator.isValidSize(1000.0));
	}

	@Test
	public void testInvalidName() {
		when(mockMeta.getContentType()).thenReturn("image/jpg");
		when(mockMeta.getFileName()).thenReturn("test$%*(&#.jpeg");
		assertFalse(imageValidator.isValid(mockMeta));
		assertEquals(WebConstants.INVALID_ENTITY_NAME_MESSAGE, imageValidator.getInvalidMessage());
	}

	@Test
	public void testNonImage() {
		when(mockMeta.getContentType()).thenReturn("text/plain");
		when(mockMeta.getFileName()).thenReturn("test.txt");
		assertFalse(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(imageValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("application/pdf");
		when(mockMeta.getFileName()).thenReturn("test.pdf");
		assertFalse(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(imageValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("text/csv");
		when(mockMeta.getFileName()).thenReturn("test.csv");
		assertFalse(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(imageValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("text/tab-separated-values");
		when(mockMeta.getFileName()).thenReturn("test.tab-separated-values");
		assertFalse(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(imageValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("text");
		when(mockMeta.getFileName()).thenReturn("test");
		assertFalse(imageValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(imageValidator.isValid(mockMeta));
	}


}
