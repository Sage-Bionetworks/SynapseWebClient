package org.sagebionetworks.web.unitclient.widget.entity.row;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.schema.TYPE;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.widget.entity.AdapterUtils;








import static org.mockito.Mockito.*;

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
	
	@Test
	public void testGetEntityForBadgeInfoProject() throws JSONObjectAdapterException {
		AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
		
		Project project = new Project();
		project.setName("very cool project");
		project.setCreatedBy("very cool creator");
		project.setCreatedOn(new Date(2112112112));
		Project result;
		result = (Project) AdapterUtils.getEntityForBadgeInfo(adapterFactory, Project.class.getName(), EntityFactory.createJSONStringForEntity(project));
		
		assertEquals(project.getName(), result.getName());
		assertEquals(project.getCreatedBy(), result.getCreatedBy());
		assertEquals(project.getCreatedOn(), result.getCreatedOn());
	}
	
	@Test
	public void testGetEntityForBadgeInfoFolder() throws JSONObjectAdapterException {
		AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
		
		Folder folder = new Folder();
		folder.setName("very cool project");
		folder.setCreatedBy("very cool creator");
		folder.setCreatedOn(new Date(2112112112));
		Folder result;
		result = (Folder) AdapterUtils.getEntityForBadgeInfo(adapterFactory, Folder.class.getName(), EntityFactory.createJSONStringForEntity(folder));
		
		assertEquals(folder.getName(), result.getName());
		assertEquals(folder.getCreatedBy(), result.getCreatedBy());
		assertEquals(folder.getCreatedOn(), result.getCreatedOn());
	}
	
	@Test
	public void testGetEntityForBadgeInfoFile() throws JSONObjectAdapterException {
		AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
		
		FileEntity file = new FileEntity();
		file.setName("very cool project");
		file.setCreatedBy("very cool creator");
		file.setCreatedOn(new Date(2112112112));
		FileEntity result;
		result = (FileEntity) AdapterUtils.getEntityForBadgeInfo(adapterFactory, FileEntity.class.getName(), EntityFactory.createJSONStringForEntity(file));
		
		assertEquals(file.getName(), result.getName());
		assertEquals(file.getCreatedBy(), result.getCreatedBy());
		assertEquals(file.getCreatedOn(), result.getCreatedOn());
	}
	
	@Test
	public void testGetEntityForBadgeInfoNotHappyCase() throws JSONObjectAdapterException {
		AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
		
		Data data = new Data();
		data.setName("very cool project");
		data.setCreatedBy("very cool creator");
		data.setCreatedOn(new Date(2112112112));
		Data result;
		result = (Data) AdapterUtils.getEntityForBadgeInfo(adapterFactory, Data.class.getName(), EntityFactory.createJSONStringForEntity(data));
		
		assertEquals(result, null);
	}
	
}
