package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.RegisterConstants;
import org.sagebionetworks.repo.model.registry.EntityRegistry;
import org.sagebionetworks.repo.model.registry.EntityTypeMetadata;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntitySchemaCacheImpl;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.shared.EntityType;

public class EntityTypeProviderTest {

	private static final String DEFAULT = "DEFAULT";
	private static final String CHILD_NAME = "Child";
	private static final String COM_SUPERRAD_MODEL_GRANDFATHER = "com.superrad.model.Grandfather";
	private static final String COM_SUPERRAD_MODEL_FATHER = "com.superrad.model.Father";
	private static final String COM_SUPERRAD_MODEL_MOTHER = "com.superrad.model.Mother";
	private static final String COM_SUPERRAD_MODEL_CHILD = "com.superrad.model.Child";
	private static final String ROOT_PATH = "/root";
	EntityTypeProvider entityTypeProvider;
	RegisterConstants mockRegisterConstants;
	AdapterFactory adapterFactory;
	EntitySchemaCache entitySchemaCache;
	String encodedRegistryJson;

	public EntityTypeProviderTest() throws Exception {
		encodedRegistryJson = createFakeRegistry();
		encodedRegistryJson = new String(Base64.encodeBase64(encodedRegistryJson.getBytes("UTF-8")), "UTF-8");
	}
	
	@Before
	public void setup() throws Exception {	
		mockRegisterConstants = mock(RegisterConstants.class);		
		when(mockRegisterConstants.getRegisterJson()).thenReturn(encodedRegistryJson);		
		adapterFactory = new AdapterFactoryImpl();
		entitySchemaCache = new EntitySchemaCacheImpl(adapterFactory);		
		entityTypeProvider = new EntityTypeProvider(mockRegisterConstants, adapterFactory, entitySchemaCache);
	}

	@Test
	public void testgetEntityTypes(){
		List<EntityType> entityTypes = entityTypeProvider.getEntityTypes();
		assertTrue(entityTypes.size() > 0);
				
		EntityType grandfather = null;
		EntityType father = null;
		EntityType mother = null;
		EntityType child = null;
		
		// Test specific types to make sure hierarchy was built properly.
		for(EntityType type : entityTypes) {
			if(COM_SUPERRAD_MODEL_GRANDFATHER.equals(type.getClassName())) {
				grandfather = type;
			} else if(COM_SUPERRAD_MODEL_FATHER.equals(type.getClassName())) {
				father = type;
			} else if(COM_SUPERRAD_MODEL_MOTHER.equals(type.getClassName())) {
				mother = type;
			} else if(COM_SUPERRAD_MODEL_CHILD.equals(type.getClassName())) {
				child = type;
			}  
		}
		assertNotNull(grandfather);
		assertNotNull(father);
		assertNotNull(mother);
		assertNotNull(child);		
		
		// test child for parents
		assertEquals(CHILD_NAME, child.getName());
		assertEquals(ROOT_PATH, child.getDefaultParentPath());
		assertEquals(COM_SUPERRAD_MODEL_CHILD, child.getClassName());
		assertTrue(child.getAliases().size() > 0);
		
		assertTrue(child.getValidParentTypes().contains(father));
		assertTrue(child.getValidParentTypes().contains(mother));
		assertEquals(2, child.getValidParentTypes().size());
		
		assertNull(child.getValidChildTypes());

		// test a grandparent for children
		assertEquals(COM_SUPERRAD_MODEL_GRANDFATHER, grandfather.getClassName());
		assertTrue(grandfather.getAliases().size() > 0);
		
		assertTrue(grandfather.getValidChildTypes().contains(father));
		assertTrue(grandfather.getValidChildTypes().contains(mother));		
		assertTrue(grandfather.getValidChildTypes().contains(grandfather));
		assertEquals(3, grandfather.getValidChildTypes().size());
		
		assertTrue(grandfather.getValidParentTypes().contains(grandfather));
		assertEquals(1, grandfather.getValidParentTypes().size());


	}
		
	private String createFakeRegistry() throws Exception {
		List<EntityTypeMetadata> types = new ArrayList<EntityTypeMetadata>();		
		EntityTypeMetadata meta;
		
		// create incestual family tree with funky god-like grandfather
		
		// Grandfather
		meta = new EntityTypeMetadata();
		meta.setName("Grandfather");	
		meta.setDefaultParentPath(ROOT_PATH);
		meta.setEntityType(COM_SUPERRAD_MODEL_GRANDFATHER);
		meta.setValidParentTypes(Arrays.asList(new String [] {COM_SUPERRAD_MODEL_GRANDFATHER, DEFAULT}));
		meta.setAliases(Arrays.asList(new String [] {"Grandpa"}));
		types.add(meta);
		
		// Father
		meta = new EntityTypeMetadata();
		meta.setName("Father");	
		meta.setDefaultParentPath(ROOT_PATH);
		meta.setEntityType(COM_SUPERRAD_MODEL_FATHER);
		meta.setValidParentTypes(Arrays.asList(new String [] {COM_SUPERRAD_MODEL_GRANDFATHER}));
		meta.setAliases(Arrays.asList(new String [] {"Dad"}));
		types.add(meta);
		
		// Mother
		meta = new EntityTypeMetadata();
		meta.setName("Mother");	
		meta.setDefaultParentPath(ROOT_PATH);
		meta.setEntityType(COM_SUPERRAD_MODEL_MOTHER);
		meta.setValidParentTypes(Arrays.asList(new String [] {COM_SUPERRAD_MODEL_GRANDFATHER}));
		meta.setAliases(Arrays.asList(new String [] {"Mom"}));
		types.add(meta);
		
		// Child
		meta = new EntityTypeMetadata();
		meta.setName(CHILD_NAME);	
		meta.setDefaultParentPath(ROOT_PATH);
		meta.setEntityType(COM_SUPERRAD_MODEL_CHILD);
		meta.setValidParentTypes(Arrays.asList(new String [] {COM_SUPERRAD_MODEL_FATHER, COM_SUPERRAD_MODEL_MOTHER}));
		meta.setAliases(Arrays.asList(new String [] {"critter"}));
		types.add(meta);	
		
		EntityRegistry registry = new EntityRegistry();
		registry.setEntityTypes(types);
		
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();		
		registry.writeToJSONObject(adapter);
		return adapter.toJSONString();
	}

}
