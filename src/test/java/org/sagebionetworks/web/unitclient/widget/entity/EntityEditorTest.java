package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntitySchemaCacheImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialog.Callback;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the entity editor.
 * @author jmhill
 *
 */
public class EntityEditorTest {

	EntityEditorDialog mockDialog;
	EntitySchemaCache schemaCache;
	AdapterFactory factory;
	ClientLogger mockLogger;
	GlobalApplicationState mockGlobal;
	PlaceChanger mockPlaceChanger;
	SynapseClientAsync mockSynapseClient;
	AutoGenFactory autoGenFactory;
	ObjectSchema versionableSchema;
	ObjectSchema schema;
	
	EntityEditor editor;
	
	@Before
	public void before() throws JSONObjectAdapterException{

		
		mockDialog = Mockito.mock(EntityEditorDialog.class);
		factory = new AdapterFactoryImpl();
		autoGenFactory = new AutoGenFactory();
		schemaCache = new EntitySchemaCacheImpl(factory);
		mockPlaceChanger = Mockito.mock(PlaceChanger.class);
		mockLogger = Mockito.mock(ClientLogger.class);
		mockGlobal = Mockito.mock(GlobalApplicationState.class);
		when(mockGlobal.getPlaceChanger()).thenReturn(mockPlaceChanger);
		
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		// setup the entity editor with 
		editor = new EntityEditor(schemaCache, factory, autoGenFactory, mockDialog, mockLogger, mockGlobal, mockSynapseClient);
	
		schema = schemaCache.getEntitySchema(ExampleEntity.EFFECTIVE_SCHEMA, ExampleEntity.class);
		versionableSchema = schemaCache.getEntitySchema(Versionable.EFFECTIVE_SCHEMA, Versionable.class);
	}
	
	@Test
	public void testCreateFilter(){
		// Make sure we get the filter we expect
		Set<String> filter = editor.createFilter(schema);
		assertNotNull(filter);
		// Validate some of the entires
		assertTrue(filter.contains("versionLabel"));
		assertTrue(filter.contains("etag"));
		assertTrue(filter.contains("accessControlList"));
		assertTrue(filter.contains("modifiedOn"));
		assertTrue(filter.contains("references"));
		assertTrue(filter.contains("environmentDescriptors"));
	}
	@Test
	public void testCopyAnnotations(){
		Annotations annos = new Annotations();
		annos.addAnnotation("key", "one");
		annos.addAnnotation("two", new Long(134));
		Annotations copy = editor.copyAnnotations(annos);
		assertFalse("A new instances should have been created",annos == copy);
		// They should have the same data.
		assertEquals(annos, copy);
	}
	
	@Test
	public void testCopyEntityToAdapter() throws JSONObjectAdapterException{
		ExampleEntity entity = new ExampleEntity();
		entity.setName("to copy");
		entity.setDescription("A very long description");
		entity.setEntityType(ExampleEntity.class.getName());
		JSONObjectAdapter adapter = editor.copyEntityToAdapter(entity);
		assertNotNull(adapter);
		ExampleEntity clone = new ExampleEntity();
		clone.initializeFromJSONObject(adapter);
		assertEquals(entity, clone);
	}

	
	@Test
	public void testCreateNewEntity(){
		EntityBundle bundle = editor.createNewEntity(ExampleEntity.class.getName(), "syn123");
		assertNotNull(bundle);
		assertNotNull(bundle.getEntity());
		assertEquals("syn123", bundle.getEntity().getParentId());
		assertEquals(ExampleEntity.class.getName(), bundle.getEntity().getEntityType());
	}
	
	@Test
	public void testonSaveEntity(){
		ExampleEntity entity = new ExampleEntity();
		entity.setName("to copy");
		entity.setId("syn123");
		entity.setDescription("A very long description");
		entity.setEntityType(ExampleEntity.class.getName());
		JSONObjectAdapter newAdapter = editor.copyEntityToAdapter(entity);
		
		Annotations annos = new Annotations();
		annos.addAnnotation("key", "one");
		annos.addAnnotation("two", new Long(134));
		Annotations newAnnos = editor.copyAnnotations(annos);
		
		// This is an onsuccess
//		AsyncMockStubber.callSuccessWith(entity.getId()).when(mockSynapseClient).createOrUpdateEntity(any(String.class), any(String.class), any(Boolean.class), any(AsyncCallback.class));
		
		// Now make the call
		editor.onSaveEntity(newAdapter, newAnnos, false);
		
//		verify(mockPlaceChanger).goTo(any(Place.class));
	}
	
	
	private EntityBundle createNewBundle(){
		ExampleEntity entity = new ExampleEntity();
		entity.setEntityType(ExampleEntity.class.getName());
		entity.setName("My Name");
		entity.setCreatedBy("ByGeorge!");
		Annotations annos = new Annotations();
		annos.addAnnotation("stringKey", "a big string");
		return  new EntityBundle(entity, annos, null, null, null);
	}
	

}
