package org.sagebionetworks.web.unitclient.widget.entity.row;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.sagebionetworks.schema.TYPE;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.widget.entity.AdapterUtils;

public class AdapterUtilsTest {
	
	@Test
	public void testNullRoundTrip(){
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.NUMBER;
		Double value = null;
		// Set the value
		AdapterUtils.setValue(adapter, type, key, value);
		// Get the value
		Double back = AdapterUtils.getValue(adapter, type, key, Double.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testNullLIstRoundTrip(){
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.NUMBER;
		List<Double> value = null;
		// Set the value
		AdapterUtils.setListValue(adapter, type, key, value, Double.class);
		// Get the value
		List<Double> back = AdapterUtils.getListValue(adapter, type, key, Double.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testDoubleRoundTrip(){
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.NUMBER;
		Double value = new Double(123.456);
		// Set the value
		AdapterUtils.setValue(adapter, type, key, value);
		// Get the value
		Double back = AdapterUtils.getValue(adapter, type, key, Double.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testLongRoundTrip(){
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.INTEGER;
		Long value = new Long(123);
		// Set the value
		AdapterUtils.setValue(adapter, type, key, value);
		// Get the value
		Long back = AdapterUtils.getValue(adapter, type, key, Long.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testStringRoundTrip(){
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.STRING;
		String value = "Some string value";
		// Set the value
		AdapterUtils.setValue(adapter, type, key, value);
		// Get the value
		String back = AdapterUtils.getValue(adapter, type, key, String.class);
		assertEquals(value, back);
	}

	@Test
	public void testDateAsStringRoundTrip() throws JSONObjectAdapterException{
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.STRING;
		long now = System.currentTimeMillis();
		Date value = new Date(now);
		// Set the value
		AdapterUtils.setValue(adapter, type, key, value);
		// Make sure the value in in a string
		assertNotNull(adapter.getString(key));
		// Get the value
		Date back = AdapterUtils.getValue(adapter, type, key, Date.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testDateAsLongRoundTrip() throws JSONObjectAdapterException{
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.INTEGER;
		long now = System.currentTimeMillis();
		Date value = new Date(now);
		// Set the value
		AdapterUtils.setValue(adapter, type, key, value);
		// Make sure the value in in a long
		assertEquals(now, adapter.getLong(key));
		// Get the value
		Date back = AdapterUtils.getValue(adapter, type, key, Date.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testDoubleListRoundTrip(){
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.NUMBER;
		List<Double> value = new ArrayList<Double>();
		value.add(new Double(123.456));
		value.add(new Double(3.14));
		// Set the value
		AdapterUtils.setListValue(adapter, type, key, value, Double.class);
		// Get the value
		List<Double> back = AdapterUtils.getListValue(adapter, type, key, Double.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testLongListRoundTrip(){
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.NUMBER;
		List<Long> value = new ArrayList<Long>();
		value.add(new Long(123));
		value.add(new Long(456));
		// Set the value
		AdapterUtils.setListValue(adapter, type, key, value, Long.class);
		// Get the value
		List<Long> back = AdapterUtils.getListValue(adapter, type, key, Long.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testStringListRoundTrip(){
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.STRING;
		List<String> value = new ArrayList<String>();
		value.add("one");
		value.add("two");
		// Set the value
		AdapterUtils.setListValue(adapter, type, key, value, String.class);
		// Get the value
		List<String> back = AdapterUtils.getListValue(adapter, type, key, String.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testDateListAsStringRoundTrip() throws JSONObjectAdapterException{
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.STRING;
		long now = System.currentTimeMillis();
		List<Date> value = new ArrayList<Date>();
		value.add(new Date(now));
		value.add(new Date(now-1));
		// Set the value
		AdapterUtils.setListValue(adapter, type, key, value, Date.class);
		// Make sure the value in in a string
		assertNotNull(adapter.getJSONArray(key));
		assertNotNull(adapter.getJSONArray(key).getString(0));
		// Get the value
		List<Date> back = AdapterUtils.getListValue(adapter, type, key, Date.class);
		assertEquals(value, back);
	}
	
	@Test
	public void testDateListAsLongRoundTrip() throws JSONObjectAdapterException{
		String key = "someKey";
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		TYPE type = TYPE.INTEGER;
		long now = System.currentTimeMillis();
		List<Date> value = new ArrayList<Date>();
		value.add(new Date(now));
		value.add(new Date(now-1));
		// Set the value
		AdapterUtils.setListValue(adapter, type, key, value, Date.class);
		// Make sure the value in in a string
		assertNotNull(adapter.getJSONArray(key));
		assertEquals(now, adapter.getJSONArray(key).getLong(0));
		// Get the value
		List<Date> back = AdapterUtils.getListValue(adapter, type, key, Date.class);
		assertEquals(value, back);
	}
	
}
