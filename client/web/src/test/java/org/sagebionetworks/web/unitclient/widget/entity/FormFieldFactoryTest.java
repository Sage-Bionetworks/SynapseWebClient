package org.sagebionetworks.web.unitclient.widget.entity;

import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntitySchemaCacheImpl;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;

import com.google.gwt.dev.util.collect.HashSet;

/**
 * Test for the FormFieldFactory.
 * @author jmhill
 *
 */
public class FormFieldFactoryTest {
	
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
	

	@Ignore // Not working in a unit test.
	@Test
	public void testString() throws JSONObjectAdapterException{
//		JSONObjectAdapter adapter = adapterFactory.createNew();
//		String key = "key";
//		EntityRowString row = (EntityRowString) EntityRowFactory.createRow(adapter, new ObjectSchema(TYPE.STRING), key);
//		// Create a field for this row
//		Field<?> field = FormFieldFactory.createField(row);
//		assertNotNull(field);
//		assertTrue(field instanceof TextField);
//		TextField<String> text = (TextField<String>) field;
//		// Make sure we can set the value
//		text.setValue("new value");
//		assertTrue(adapter.has(key));
//		assertEquals("new value", adapter.getString(key));

	}
}
