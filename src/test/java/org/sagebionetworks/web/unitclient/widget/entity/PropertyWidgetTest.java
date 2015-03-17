package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntitySchemaCacheImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.client.widget.entity.AnnotationsWidget;
import org.sagebionetworks.web.client.widget.entity.AnnotationsWidgetView;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class PropertyWidgetTest {

	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	AnnotationsWidgetView mockView;
	
	JSONObjectAdapter jsonObjectAdapter;
	static AdapterFactoryImpl adapterFactory = new AdapterFactoryImpl();
	static EntitySchemaCache schemaCache = new EntitySchemaCacheImpl(adapterFactory);
	
	AnnotationsWidget propertyWidget;
	EntityBundle testBundle;
	Annotations annotations;
	EntityUpdatedHandler mockEntityUpdatedHandler;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(AnnotationsWidgetView.class);
		mockEntityUpdatedHandler = mock(EntityUpdatedHandler.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		propertyWidget = new AnnotationsWidget(mockView, adapterFactory, schemaCache, mockSynapseClient, jsonObjectAdapter, mockGlobalApplicationState, mockAuthenticationController);
		
		testBundle = mock(EntityBundle.class);
		FileEntity file = new FileEntity();
		
		when(testBundle.getEntity()).thenReturn(file);
		annotations = new Annotations();
		when(testBundle.getAnnotations()).thenReturn(annotations);
		propertyWidget.configure(testBundle, true);
		propertyWidget.setEntityUpdatedHandler(mockEntityUpdatedHandler);
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigure() {
		//verifying that configuring the widget calls configure on the view
		verify(mockView).configure(any(List.class), anyBoolean());
	}
	
	@Test
	public void testFireEntityUpdatedEvent() {
		propertyWidget.fireEntityUpdatedEvent();
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testUpdateEntity() {
		propertyWidget.updateEntity();
		//if an entity is updated, verify that event would have been sent back
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testUpdateEntityFailure() {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		propertyWidget.updateEntity();
		
		verify(mockView).showErrorMessage(anyString());
	}
	
	private static EntityRow getTestMockEntityRow(String key, String value) {
		EntityRow testRow = mock(EntityRow.class);
		when(testRow.getLabel()).thenReturn(key);
		when(testRow.getValue()).thenReturn(value);
		return testRow;
	}
	
	@Test
	public void testDeleteAnnotation() {
		String key = "myTestAnnotation";
		String value = "myValue";
		
		EntityRow mockRow = getTestMockEntityRow(key, value);
		annotations.addAnnotation(mockRow.getLabel(), mockRow.getValue());
		//verify that it can be found in the annotations beforehand
		assertTrue(annotations.keySet().contains(mockRow.getLabel()));
		//make the call
		propertyWidget.deleteAnnotation(mockRow);
		//verify it was removed from the annotations
		assertFalse(annotations.keySet().contains(mockRow.getLabel()));
		//and the entity was updated
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testUpdateAnnotation() {
		String key = "myTestAnnotation";
		String value1 = "myValue1";
		String value2 = "myValue2";
		
		//annotation starts as value1
		annotations.addAnnotation(key, value1);
		assertEquals(value1,  annotations.getStringAnnotations().get(key).get(0));
		EntityRow mockRow = getTestMockEntityRow(key, value2);
		//change to value2
		propertyWidget.updateAnnotation(mockRow);
		//verify it was updated
		assertEquals(value2,  annotations.getStringAnnotations().get(key).get(0));
		
		//and the entity was updated
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testAddAnnotation() {
		String key = "myTestAnnotation";
		ANNOTATION_TYPE type = ANNOTATION_TYPE.DOUBLE;
		assertTrue(annotations.getDoubleAnnotations().isEmpty());
		propertyWidget.addAnnotation(key, type);
		//verify there is a new annotation
		assertFalse(annotations.getDoubleAnnotations().isEmpty());
		
		//and the entity was updated
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
}
