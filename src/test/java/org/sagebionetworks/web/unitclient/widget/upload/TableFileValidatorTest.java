package org.sagebionetworks.web.unitclient.widget.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.TableFileValidator;
import org.sagebionetworks.web.shared.WebConstants;

public class TableFileValidatorTest {

	TableFileValidator tableValidator;
	FileMetadata mockMeta;

	@Before
	public void setup() {
		tableValidator = new TableFileValidator();
		mockMeta = mock(FileMetadata.class);
	}

	@Test
	public void testTable() {
		when(mockMeta.getContentType()).thenReturn("text/csv");
		when(mockMeta.getFileName()).thenReturn("test.csv");
		assertTrue(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertTrue(tableValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("text/tab-separated-values");
		when(mockMeta.getFileName()).thenReturn("test.tab-separated-values");
		assertTrue(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertTrue(tableValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("text/plain");
		when(mockMeta.getFileName()).thenReturn("test.txt");
		assertTrue(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertTrue(tableValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("text/tsv");
		when(mockMeta.getFileName()).thenReturn("test.tsv");
		assertTrue(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertTrue(tableValidator.isValid(mockMeta));

	}

	@Test
	public void testInvalidName() {
		when(mockMeta.getContentType()).thenReturn("image/jpg");
		when(mockMeta.getFileName()).thenReturn("test$%*(&#.jpeg");
		assertFalse(tableValidator.isValid(mockMeta));
		assertEquals(WebConstants.INVALID_ENTITY_NAME_MESSAGE, tableValidator.getInvalidMessage());
	}

	@Test
	public void testNotTable() {
		when(mockMeta.getContentType()).thenReturn("image/jpg");
		when(mockMeta.getFileName()).thenReturn("test.jpg");
		assertFalse(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(tableValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("image/png");
		when(mockMeta.getFileName()).thenReturn("test.png");
		assertFalse(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(tableValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("image/gif");
		when(mockMeta.getFileName()).thenReturn("test.gif");
		assertFalse(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(tableValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("image/jpe");
		when(mockMeta.getFileName()).thenReturn("test.jpe");
		assertFalse(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(tableValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("image/pjpeg");
		when(mockMeta.getFileName()).thenReturn("test.pjpeg");
		assertFalse(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(tableValidator.isValid(mockMeta));

		when(mockMeta.getContentType()).thenReturn("application/pdf");
		when(mockMeta.getFileName()).thenReturn("test.pdf");
		assertFalse(tableValidator.isValid(mockMeta));
		when(mockMeta.getContentType()).thenReturn(null);
		assertFalse(tableValidator.isValid(mockMeta));
	}
}
