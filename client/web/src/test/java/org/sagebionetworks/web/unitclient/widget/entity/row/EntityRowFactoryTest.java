package org.sagebionetworks.web.unitclient.widget.entity.row;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.schema.FORMAT;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.TYPE;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntitySchemaCacheImpl;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowDateAsLong;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowDateAsString;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowDouble;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowFactory;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowLong;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowString;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowList;

import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.editor.client.Editor.Ignore;

public class EntityRowFactoryTest {
	
	// Use the schema cache
	static AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
	static EntitySchemaCache schemaCache = new EntitySchemaCacheImpl(new JSONEntityFactoryImpl(adapterFactory));
	ExampleEntity entity;
	ObjectSchema schema;
	Annotations annos;
	Set<String> filter;
	
	ObjectSchema entityInterfaceSchema;
	
	@Before
	public void before(){
		entity = new ExampleEntity();
		schema = schemaCache.getSchemaEntity(entity);
		entityInterfaceSchema = schemaCache.getEntitySchema(Entity.EFFECTIVE_SCHEMA, Entity.class);
		annos = new Annotations();
		filter = new HashSet<String>();
	}
	
	
	@Test (expected=IllegalArgumentException.class) 
	public void testNullAdapter(){
		EntityRowFactory.createRow(null, new ObjectSchema(), "key");
	}
	
	@Test (expected=IllegalArgumentException.class) 
	public void testNullSchema(){
		EntityRowFactory.createRow(adapterFactory.createNew(), null, "key");
	}
	
	@Test (expected=IllegalArgumentException.class) 
	public void testNullKey(){
		EntityRowFactory.createRow(adapterFactory.createNew(), new ObjectSchema(), null);
	}
	
	@Test
	public void testString() throws JSONObjectAdapterException{
		JSONObjectAdapter adapter = adapterFactory.createNew();
		EntityRow<?> row = EntityRowFactory.createRow(adapter, new ObjectSchema(TYPE.STRING), "key");
		assertTrue(row instanceof EntityRowString);
		EntityRowString stringRow = (EntityRowString) row;
		// check the wiring
		stringRow.setValue("test");
		assertEquals("test", adapter.getString("key"));
		assertEquals("test", stringRow.getValue());
	}
	
	@Test
	public void testDateAsLong() throws JSONObjectAdapterException{
		JSONObjectAdapter adapter = adapterFactory.createNew();
		ObjectSchema schema = new ObjectSchema(TYPE.INTEGER);
		schema.setFormat(FORMAT.UTC_MILLISEC);
		EntityRow<?> row = EntityRowFactory.createRow(adapter, schema, "key");
		assertTrue(row instanceof EntityRowDateAsLong);
		EntityRowDateAsLong dateRow = (EntityRowDateAsLong) row;
		// check the wiring
		long now = 1331069728612l;
		System.out.println(now);
		dateRow.setValue(new Date(now));
		assertEquals(1331069728612l, adapter.getLong("key"));
		assertEquals(new Date(now), dateRow.getValue());
		
		// Test null
		dateRow.setValue(null);
		assertEquals(null, dateRow.getValue());
	}
	
	@Test
	public void testLong() throws JSONObjectAdapterException{
		JSONObjectAdapter adapter = adapterFactory.createNew();
		ObjectSchema schema = new ObjectSchema(TYPE.INTEGER);
		EntityRow<?> row = EntityRowFactory.createRow(adapter, schema, "key");
		assertTrue(row instanceof EntityRowLong);
		EntityRowLong longRow = (EntityRowLong) row;
		// check the wiring
		long value = 1331069728612l;
		longRow.setValue(value);
		assertEquals(1331069728612l, adapter.getLong("key"));
		assertEquals(new Long(value), longRow.getValue());
		
		// Test null
		longRow.setValue(null);
		assertEquals(null, longRow.getValue());
	}
	
	@Test
	public void testDouble() throws JSONObjectAdapterException{
		JSONObjectAdapter adapter = adapterFactory.createNew();
		ObjectSchema schema = new ObjectSchema(TYPE.NUMBER);
		EntityRow<?> row = EntityRowFactory.createRow(adapter, schema, "key");
		assertTrue(row instanceof EntityRowDouble);
		EntityRowDouble longRow = (EntityRowDouble) row;
		// check the wiring
		double value = 1.233;
		longRow.setValue(value);
		assertEquals(new Double(value), new Double(adapter.getDouble("key")));
		assertEquals(new Double(value), longRow.getValue());
		
		// Test null
		longRow.setValue(null);
		assertEquals(null, longRow.getValue());
	}
	
	@Test
	public void testStringList() throws JSONObjectAdapterException{
		JSONObjectAdapter adapter = adapterFactory.createNew();
		ObjectSchema schema = new ObjectSchema(TYPE.ARRAY);
		schema.setItems(new ObjectSchema(TYPE.STRING));
		EntityRow<?> row = EntityRowFactory.createRow(adapter, schema, "key");
		assertTrue(row instanceof EntityRowList);
		EntityRowList<String> rowImpl = (EntityRowList<String>) row;
		// check the wiring
		List<String> value = new ArrayList<String>();
		value.add("one");
		value.add("two");
		rowImpl.setValue(value);
		assertNotNull(adapter.getJSONArray("key"));
		assertEquals(value, rowImpl.getValue());
		
		// Test null
		rowImpl.setValue(null);
		assertEquals(null, rowImpl.getValue());
	}
	
	@Test
	public void testDoubleList() throws JSONObjectAdapterException{
		JSONObjectAdapter adapter = adapterFactory.createNew();
		ObjectSchema schema = new ObjectSchema(TYPE.ARRAY);
		schema.setItems(new ObjectSchema(TYPE.NUMBER));
		EntityRow<?> row = EntityRowFactory.createRow(adapter, schema, "key");
		assertTrue(row instanceof EntityRowList);
		EntityRowList<Double> rowImpl = (EntityRowList<Double>) row;
		// check the wiring
		List<Double> value = new ArrayList<Double>();
		value.add(1.1);
		value.add(1.2);
		rowImpl.setValue(value);
		assertNotNull(adapter.getJSONArray("key"));
		assertEquals(value, rowImpl.getValue());
		
		// Test null
		rowImpl.setValue(null);
		assertEquals(null, rowImpl.getValue());
	}
	
	@Test
	public void testLongList() throws JSONObjectAdapterException{
		JSONObjectAdapter adapter = adapterFactory.createNew();
		ObjectSchema schema = new ObjectSchema(TYPE.ARRAY);
		schema.setItems(new ObjectSchema(TYPE.INTEGER));
		EntityRow<?> row = EntityRowFactory.createRow(adapter, schema, "key");
		assertTrue(row instanceof EntityRowList);
		EntityRowList<Long> rowImpl = (EntityRowList<Long>) row;
		// check the wiring
		List<Long> value = new ArrayList<Long>();
		value.add(100l);
		value.add(200l);
		rowImpl.setValue(value);
		assertNotNull(adapter.getJSONArray("key"));
		assertEquals(value, rowImpl.getValue());
		
		// Test null
		rowImpl.setValue(null);
		assertEquals(null, rowImpl.getValue());
	}
	

	
	@Test
	public void testCreateTransientFilter(){
		ObjectSchema schema = new ObjectSchema(TYPE.OBJECT);
		schema.setProperties(new LinkedHashMap<String, ObjectSchema>());
		// Add a transient and non-transient property
		ObjectSchema transientSchema = new ObjectSchema(TYPE.STRING);
		transientSchema.setTransient(true);
		schema.getProperties().put("transient", transientSchema);
		// Add a non-transient property
		ObjectSchema nonTransientSchema = new ObjectSchema(TYPE.STRING);
		nonTransientSchema.setTransient(false);
		schema.getProperties().put("non-transient", nonTransientSchema);
		Set<String> filter = new HashSet<String>();
		EntityRowFactory.addTransientToFilter(schema, filter);
		assertNotNull(filter);
		assertTrue(filter.contains("transient"));
		assertFalse(filter.contains("non-transient"));
	}
	
	@Test
	public void testCreateObjectTypeFilter(){
		ObjectSchema schema = new ObjectSchema(TYPE.OBJECT);
		schema.setProperties(new LinkedHashMap<String, ObjectSchema>());
		// Add a non-object property
		ObjectSchema nonObject = new ObjectSchema(TYPE.STRING);
		schema.getProperties().put("nonObject", nonObject);
		// Add an object property
		ObjectSchema object = new ObjectSchema(TYPE.OBJECT);
		schema.getProperties().put("object", object);
		// Add an array of objects
		ObjectSchema arrayObjects = new ObjectSchema(TYPE.ARRAY);
		arrayObjects.setItems(new ObjectSchema(TYPE.OBJECT));
		schema.getProperties().put("arrayOfObjects", arrayObjects);
		// an array of strings
		ObjectSchema arrayString = new ObjectSchema(TYPE.ARRAY);
		arrayString.setItems(new ObjectSchema(TYPE.STRING));
		schema.getProperties().put("arrayOfStrings", arrayString);
		Set<String> filter = new HashSet<String>();
		EntityRowFactory.addObjectTypeToFilter(schema, filter);
		assertNotNull(filter);
		assertTrue(filter.contains("object"));
		assertTrue(filter.contains("arrayOfObjects"));
		assertFalse(filter.contains("nonObject"));
		assertFalse(filter.contains("arrayOfStrings"));
	}
	
	@Test
	public void testCreateRowListFiltered() throws JSONObjectAdapterException{
		// Get the adapter for this entity
		JSONObjectAdapter entityAdapter = entity.writeToJSONObject(adapterFactory.createNew());
		// Use the entity keys for a filter
		Set<String> filter = new HashSet<String>();
		EntityRowFactory.addTransientToFilter(schema, filter);
		EntityRowFactory.addObjectTypeToFilter(schema, filter);
		// We want to use the entity schema as a filter
		List<EntityRow<?>> results = EntityRowFactory.createEntityRowListForProperties(entityAdapter, schema, filter);
		// Make sure none of the filtered properties are there
		for(EntityRow<?> row: results){
			assertNotNull(row.getLabel());
//			assertFalse(filter.contains(data.getKey()));
		}

	}


}
