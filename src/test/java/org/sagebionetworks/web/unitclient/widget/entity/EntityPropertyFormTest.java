package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyForm;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyFormView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityPropertyFormTest {

	EntityPropertyFormView mockView;
	EventBus mockEventBus; 
	NodeModelCreator mockNodeModelCreator;
	SynapseClientAsync mockSynapseClient; 
	SynapseJSNIUtils mockSynapseJSNIUtils; 
	WidgetRegistrar mockWidgetRegistrar;
	EntityPropertyForm presenter;
	ExampleEntity entity;
	EntityBundle entityBundle;
	AttachmentData attachment1;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(EntityPropertyFormView.class);
		mockEventBus = mock(EventBus.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		presenter = new EntityPropertyForm(mockView, mockEventBus,
				mockNodeModelCreator, mockSynapseClient, mockSynapseJSNIUtils,
				mockWidgetRegistrar, mockGlobalApplicationState,
				mockAuthenticationController);
		EntityBundle bundle = new EntityBundle(new Project(), null, null,null,null, null, null, null);
		
		String entityId = "123";
		entity = new ExampleEntity();
		entity.setId(entityId);
		entity.setName("Test Entity");
		entity.setEntityType(ExampleEntity.class.getName());
		List<AttachmentData> entityAttachments = new ArrayList<AttachmentData>();
		String attachment1Name = "attachment1";
		attachment1 = new AttachmentData();
		attachment1.setName(attachment1Name);
		attachment1.setTokenId("token1");
		attachment1.setContentType(WidgetConstants.YOUTUBE_CONTENT_TYPE);
		entityAttachments.add(attachment1);
		entity.setAttachments(entityAttachments);
		EntityBundleTransport ebt = new EntityBundleTransport();
		ebt.setEntityJson(EntityFactory.createJSONStringForEntity(entity));
		AsyncMockStubber.callSuccessWith(ebt).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		entityBundle = new EntityBundle(entity, null, null, null, null, null, null, null);
		when(mockNodeModelCreator.createEntityBundle(any(EntityBundleTransport.class))).thenReturn(entityBundle);
		
		AdapterFactory factory = new AdapterFactoryImpl();
		JSONObjectAdapter newAdapter = factory.createNew();
		entity.writeToJSONObject(newAdapter);
		presenter.setDataCopies(newAdapter, null, null, null, bundle);
	}

	@Test
	public void testInit() {
		verify(mockEventBus).addHandler(any(GwtEvent.Type.class), any(EntityUpdatedHandler.class));
	}
	
	@Test
	public void testEdit() {
		presenter.showEditEntityDialog("", null, null, null, null, null, null);
		verify(mockView).showEditEntityDialog(anyString());
	}

	@Test
	public void testRefreshAttachments() throws Exception{
		Code newEntity = new Code();
		presenter.refreshEntityAttachments(newEntity);
		assertEquals(newEntity, presenter.getEntity());
		verify(mockView, Mockito.times(2)).refresh();
	}

	
	@Test
	public void testAttachmentRefresh() {
		presenter.refreshEntityAttachments();
		//testing reload
		verify(mockView).showLoading();
		verify(mockView).hideLoading();
		verify(mockView, Mockito.times(2)).refresh();
	}
	
	@Test
	public void testAttachmentRefreshFailure() {
		Exception caught = new Exception("test failure");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		presenter.refreshEntityAttachments();
		//testing failure
		verify(mockView).showLoading();
		verify(mockView).hideLoading();
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testEntityUpdateHandler() {
		presenter.init();

		AdapterFactory factory = new AdapterFactoryImpl();
		JSONObjectAdapter adapter = factory.createNew();
		
		ArgumentCaptor<EntityUpdatedHandler> arg = ArgumentCaptor.forClass(EntityUpdatedHandler.class);
		verify(mockEventBus).addHandler(any(GwtEvent.Type.class), arg.capture());
		//test the event handler
		
		EntityUpdatedHandler handler = arg.getValue();
		EntityUpdatedEvent testEvent = new EntityUpdatedEvent();
		//verify that synapseClient.getEntityBundle is called when the view is visible (and bundle is set), and not called if otherwise
		when(mockView.isComponentVisible()).thenReturn(false);
		handler.onPersistSuccess(testEvent);
		verify(mockSynapseClient, Mockito.times(0)).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		//clear out the entity bundle and make sure it's still not called
		when(mockView.isComponentVisible()).thenReturn(true);
		presenter.setDataCopies(adapter, null, null, null, null);
		handler.onPersistSuccess(testEvent);
		verify(mockSynapseClient, Mockito.times(0)).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		//set the bundle, but don't set the entity id
		presenter.setDataCopies(adapter, null, null, null, entityBundle);
		entityBundle.getEntity().setId(null);
		handler.onPersistSuccess(testEvent);
		verify(mockSynapseClient, Mockito.times(0)).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		//set the ID, and verify that the service is called now
		entityBundle.getEntity().setId("syn1");
		handler.onPersistSuccess(testEvent);
		verify(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
	}
}
