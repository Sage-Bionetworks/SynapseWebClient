package org.sagebionetworks.web.client;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidgetImpl;

public class StringUtilsTest {
	
	@Test
	public void testEmptyAsNullWithRealValues(){
		//SWC-4099: whitespace no longer trimmed
		assertNotEquals("a", StringUtils.emptyAsNull(" a\n "));
	}

	@Test
	public void testIsValueChangedNullNull(){
		assertFalse(StringUtils.isValueChanged(null, null));
	}
	
	@Test
	public void testIsValueChangedEmptyNull(){
		assertFalse(StringUtils.isValueChanged(" ", null));
	}
	
	@Test
	public void testIsValueChangedNullEmpty(){
		assertFalse(StringUtils.isValueChanged(null, "\t"));
	}
	
	@Test
	public void testIsValueChangedRealNull(){
		assertTrue(StringUtils.isValueChanged("a", null));
	}
	
	@Test
	public void testIsValueChangedNullReal(){
		assertTrue(StringUtils.isValueChanged(null, "a"));
	}
	
	@Test
	public void testIsValueChangedRealEmpty(){
		assertTrue(StringUtils.isValueChanged("a", ""));
	}
	
	@Test
	public void testIsValueChangedEmptyReal(){
		assertTrue(StringUtils.isValueChanged("", "a"));
	}
	
	@Test
	public void testIsValueChangedRealRealDifferent(){
		assertTrue(StringUtils.isValueChanged("b", "a"));
	}
	
	@Test
	public void testIsValueChangedRealRealSame(){
		assertFalse(StringUtils.isValueChanged("a", "a"));
	}
	
	@Test
	public void testIsValueChangedRealSpaceRealDifferent(){
		//SWC-4099: whitespace changes (that would have been trimmed previously) count as a change
		assertTrue(StringUtils.isValueChanged(" a ", "a"));
	}
	
	@Test
	public void testIsValueChangedRealRealSpaceDifferent(){
		//SWC-4099: whitespace changes (that would have been trimmed previously) count as a change
		assertTrue(StringUtils.isValueChanged(" a ", " a\t"));
	}
	
	@Test
	public void testTrimWithEmptyAsNull_Null(){
		assertEquals(null, StringUtils.emptyAsNull(null));
	}
	
	@Test
	public void testTrimWithEmptyAsNullOkay(){
		assertEquals("a", StringUtils.emptyAsNull("a"));
	}
	
	@Test
	public void testToTitleCase(){
		assertNull(StringUtils.toTitleCase(null));
		assertEquals("", StringUtils.toTitleCase(""));
		assertEquals("Hello", StringUtils.toTitleCase("hello"));
		assertEquals("Hello World", StringUtils.toTitleCase("hello world"));
		assertEquals("Hello World", StringUtils.toTitleCase("heLLo WORLD"));
	}
	
	@Test
	public void testTruncateString(){
		assertEquals(null, StringUtils.truncateValues(null, UploadPreviewWidgetImpl.MAX_CHARS_PER_CELL, false));
		assertEquals("small", StringUtils.truncateValues("small", UploadPreviewWidgetImpl.MAX_CHARS_PER_CELL, false));
		assertEquals("not so sm...", StringUtils.truncateValues("not so small", UploadPreviewWidgetImpl.MAX_CHARS_PER_CELL, false));
		assertEquals("not so sm&hellip;", StringUtils.truncateValues("not so small", UploadPreviewWidgetImpl.MAX_CHARS_PER_CELL, true));
	}
}
