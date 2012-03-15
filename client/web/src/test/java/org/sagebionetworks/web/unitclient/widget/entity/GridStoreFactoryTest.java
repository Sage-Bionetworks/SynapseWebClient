package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.GridStoreFactory;
/**
 * Test for the grid store factory
 * @author jmhill
 *
 */
public class GridStoreFactoryTest {
	
	@Test
	public void testStringValueToString(){
		String value = "This is just a string and should remain so";
		String result = GridStoreFactory.valueToString(value);
		assertEquals(value, result);
	}
	
	@Test
	public void testTooLongStringValueToString(){
		String value = "This is just a string and should remain so but is so long that it will be truncated";
		String result = GridStoreFactory.valueToString(value);
		assertEquals(value.substring(0, GridStoreFactory.MAX_CHARS_IN_LIST-1), result);
	}
	
	@Test
	public void testLongValueToString(){
		Long value = new Long(4455);
		String result = GridStoreFactory.valueToString(value);
		assertEquals("4455", result);
	}

	@Test
	public void testDoubleValueToString(){
		Double value = new Double(4455.99);
		String result = GridStoreFactory.valueToString(value);
		assertEquals("4455.99", result);
	}
	
	@Test
	public void testListStringValueToString(){
		List<String> value = new ArrayList<String>();
		value.add("one");
		value.add("two");
		value.add("three");
		String result = GridStoreFactory.valueToString(value);
		assertEquals("one, two, three", result);
	}
	
	@Test
	public void testListTooLongStringValueToString(){
		List<String> value = new ArrayList<String>();
		for(int i=0; i<100; i++){
			value.add("value"+i);
		}
		String result = GridStoreFactory.valueToString(value);
		assertEquals("value0, value1, value2, value3, value4, value5, value6", result);
	}
	
	@Test
	public void testValueToToolTips(){
		String value = "This is just a string and should remain so but is so long that it will be truncated but not in the tooltips ";
		String result = GridStoreFactory.valueToToolTips(value);
		// Tool tips should not be truncated.
		assertEquals(value, result);
	}
	
	@Test
	public void testListStringValueToToolTips(){
		List<String> value = new ArrayList<String>();
		for(int i=0; i<8; i++){
			value.add("value"+i);
		}
		String result = GridStoreFactory.valueToToolTips(value);
		System.out.println(result);
		assertEquals("<ul><li>value0</li><li>value1</li><li>value2</li><li>value3</li><li>value4</li><li>value5</li><li>value6</li><li>value7</li></ul>", result);
	}
	
}
