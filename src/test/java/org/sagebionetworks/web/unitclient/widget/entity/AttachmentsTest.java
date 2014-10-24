package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.Attachments;
import org.sagebionetworks.web.client.widget.entity.AttachmentsView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WidgetConstants;

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
		
		// setup the entity editor with 
		presenter = new Attachments(mockView, mockSynapseClient, mockGlobalAppState, mockAuthenticationController, mockNodeModelCreator, mockJSONObjectAdapter, mockEventBus, mockWidgetRegistrar, mockWidgetEditor);
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
