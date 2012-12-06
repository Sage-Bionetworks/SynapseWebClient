package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.contains;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntitySchemaCacheImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.Attachments;
import org.sagebionetworks.web.client.widget.entity.AttachmentsView;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialog;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the entity editor.
 * @author jmhill
 *
 */
public class AttachmentsTest {

	Attachments presenter;
	AttachmentsView mockView;
	GlobalApplicationState mockGlobalAppState;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	JSONObjectAdapter mockJSONObjectAdapter;
	EventBus mockEventBus;
	WidgetRegistrar mockWidgetRegistrar;
	BaseEditWidgetDescriptorPresenter mockWidgetEditor;
	ExampleEntity entity;
	AttachmentData attachment1, attachment2;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockGlobalAppState = Mockito.mock(GlobalApplicationState.class);
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		mockNodeModelCreator = Mockito.mock(NodeModelCreator.class);
		mockJSONObjectAdapter = Mockito.mock(JSONObjectAdapter.class);
		mockEventBus = Mockito.mock(EventBus.class);
		mockWidgetRegistrar = Mockito.mock(WidgetRegistrar.class);
		mockWidgetEditor = Mockito.mock(BaseEditWidgetDescriptorPresenter.class);
		mockView = Mockito.mock(AttachmentsView.class);
		// Setup the the entity
		String entityId = "123";
		entity = new ExampleEntity();
		entity.setId(entityId);
		entity.setEntityType(ExampleEntity.class.getName());
		List<AttachmentData> entityAttachments = new ArrayList<AttachmentData>();
		String attachment1Name = "attachment1";
		attachment1 = new AttachmentData();
		attachment1.setName(attachment1Name);
		attachment1.setTokenId("token1");
		attachment1.setContentType(WidgetConstants.YOUTUBE_CONTENT_TYPE);
		attachment2 = new AttachmentData();
		attachment2.setName("attachment2");
		attachment2.setTokenId("token2");
		String otherContentType = "application/binary";
		attachment2.setContentType(otherContentType);
		entityAttachments.add(attachment1);
		entityAttachments.add(attachment2);
		entity.setAttachments(entityAttachments);
		when(mockJSONObjectAdapter.createNew()).thenReturn(new JSONObjectAdapterImpl());
		when(mockWidgetRegistrar.isWidgetContentType(eq(WidgetConstants.YOUTUBE_CONTENT_TYPE))).thenReturn(true);
		when(mockWidgetRegistrar.isWidgetContentType(eq(otherContentType))).thenReturn(false);
		
		// setup the entity editor with 
		presenter = new Attachments(mockView, mockSynapseClient, mockGlobalAppState, mockAuthenticationController, mockNodeModelCreator, mockJSONObjectAdapter, mockEventBus, mockWidgetRegistrar, mockWidgetEditor);
	}
	
	@Test
	public void testConfigureWidgetAttachments() {
		boolean showWidgetAttachments = true;
		presenter.configure("", entity, showWidgetAttachments);
		verify(mockView).configure(anyString(), eq(entity.getId()), any(List.class), eq(true));
		//and verify the working set of attachments contains only the first attachment (the widget).
		List<AttachmentData> workingSet = presenter.getWorkingSet();
		assertTrue(workingSet.size() == 1);
		assertEquals(attachment1, workingSet.get(0));
	}
	@Test
	public void testConfigureOtherAttachments() {
		boolean showWidgetAttachments = false;
		presenter.configure("", entity, showWidgetAttachments);
		verify(mockView).configure(anyString(), eq(entity.getId()), any(List.class), eq(false));
		//and verify the working set of attachments contains only the second attachment (not the widget).
		List<AttachmentData> workingSet = presenter.getWorkingSet();
		assertTrue(workingSet.size() == 1);
		assertEquals(attachment2, workingSet.get(0));
	}
	@Test
	public void testEdit(){
		//should be passed on to the widget editor
		presenter.configure("", entity, true);
		presenter.editAttachment(attachment1.getTokenId());
		verify(mockWidgetEditor).editExisting(eq(entity.getId()), eq(attachment1.getName()), any(List.class));
	}
	
	@Test
	public void testEditNotFound(){
		//should be passed on to the widget editor
		presenter.configure("", entity, true);
		presenter.editAttachment("invalid token");
		verify(mockWidgetEditor, Mockito.times(0)).editExisting(eq(entity.getId()), eq(attachment1.getName()), any(List.class));
	}
	
	@Test
	public void testDelete(){
		presenter.configure("", entity, true);
		presenter.deleteAttachment(attachment1.getTokenId());
		//verify that synapse client createOrUpdateEntity is called, and that attachment1 token id isn't in the json
		verify(mockSynapseClient).createOrUpdateEntity(not(contains(attachment1.getTokenId())), anyString(), anyBoolean(), any(AsyncCallback.class));
	}
	
	@Test
	public void testDeleteNotFound(){
		presenter.configure("", entity, true);
		presenter.deleteAttachment("invalid token");
		//verify that synapse client createOrUpdateEntity is called, and that attachment1 token id isn't in the json
		verify(mockSynapseClient, Mockito.times(0)).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
	}
	

}
