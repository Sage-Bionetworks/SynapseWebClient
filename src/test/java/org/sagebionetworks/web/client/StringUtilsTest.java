package org.sagebionetworks.web.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringUtilsTest {
	
	@Test
	public void testTrimWithEmptyAsNull_Trim(){
		assertEquals("a", StringUtils.trimWithEmptyAsNull(" a\n "));
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
	public void testIsValueChangedRealSpaceRealSame(){
		assertFalse(StringUtils.isValueChanged(" a ", "a"));
	}
	
	@Test
	public void testIsValueChangedRealRealSpaceSame(){
		assertFalse(StringUtils.isValueChanged(" a ", " a\t"));
	}
	
	@Test
	public void testTrimWithEmptyAsNull_Null(){
		assertEquals(null, StringUtils.trimWithEmptyAsNull(null));
	}
	
	@Test
	public void testTrimWithEmptyAsNullOkay(){
		assertEquals("a", StringUtils.trimWithEmptyAsNull("a"));
	}
}
