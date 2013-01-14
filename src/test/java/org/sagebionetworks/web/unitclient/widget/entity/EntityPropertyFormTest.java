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
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyForm;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyFormView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
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
	@Before
	public void before() throws JSONObjectAdapterException {
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(EntityPropertyFormView.class);
		mockEventBus = mock(EventBus.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		presenter = new EntityPropertyForm(mockView, mockEventBus, mockNodeModelCreator, mockSynapseClient, mockSynapseJSNIUtils, mockWidgetRegistrar);
		EntityBundle bundle = new EntityBundle(new Project(), null, null, null,null,null, null);
		
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
		entityBundle = new EntityBundle(entity, null, null, null, null, null, null);
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
	public void testPreview() throws Exception {
		final String testHtml = "<h1>HTML Returns</h1>";
		final String testMarkdown = "HTML Returns\n----------";
		AsyncMockStubber
				.callSuccessWith(testHtml)
				.when(mockSynapseClient)
				.markdown2Html(any(String.class), any(String.class), any(Boolean.class),
						any(AsyncCallback.class));
		
		presenter.showPreview(testMarkdown,  "");
		verify(mockSynapseClient).markdown2Html(any(String.class),
				any(String.class), any(Boolean.class), any(AsyncCallback.class));
		verify(mockView).showPreview(anyString(), any(EntityBundle.class),any(WidgetRegistrar.class), any(SynapseClientAsync.class), any(NodeModelCreator.class), any(JSONObjectAdapter.class));
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
	
	
}
