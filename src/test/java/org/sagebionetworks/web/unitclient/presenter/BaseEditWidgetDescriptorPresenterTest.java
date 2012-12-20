package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class BaseEditWidgetDescriptorPresenterTest {
	
	BaseEditWidgetDescriptorPresenter presenter;
	BaseEditWidgetDescriptorView mockView;
	SynapseClientAsync mockSynapse;
	NodeModelCreator mockNodeModelCreator;
	WidgetRegistrar mockWidgetRegistrar;
	JSONObjectAdapter mockJSONObjectAdapter;
	ExampleEntity entity;
	String descriptor1Json;
	YouTubeWidgetDescriptor descriptor1;
	AttachmentData attachment1, attachment2;
	
	@Before
	public void setup() throws Exception {
		mockView = mock(BaseEditWidgetDescriptorView.class);
		mockSynapse = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		mockJSONObjectAdapter = mock(JSONObjectAdapter.class);
		presenter = new BaseEditWidgetDescriptorPresenter(mockView, mockSynapse, mockNodeModelCreator, mockJSONObjectAdapter, mockWidgetRegistrar);	
		verify(mockView).setPresenter(presenter);
		
		// Setup the the entity
		String entityId = "123";
		entity = new ExampleEntity();
		entity.setId(entityId);
		entity.setEntityType(ExampleEntity.class.getName());
		List<AttachmentData> attachments = new ArrayList<AttachmentData>();
		String attachment1Name = "attachment1";
		attachment1 = new AttachmentData();
		attachment1.setName(attachment1Name);
		attachment1.setContentType(WidgetConstants.YOUTUBE_CONTENT_TYPE);
		attachment2 = new AttachmentData();
		attachment2.setName("attachment2");
		attachments.add(attachment1);
		attachments.add(attachment2);
		entity.setAttachments(attachments);
		
		descriptor1 = new YouTubeWidgetDescriptor();
		descriptor1.setVideoId("myVideoId");
		descriptor1Json = EntityFactory.createJSONStringForEntity(descriptor1);
		AsyncMockStubber.callSuccessWith(descriptor1Json).when(mockSynapse).getWidgetDescriptorJson(eq(entityId), eq(attachment1Name), any(AsyncCallback.class));
		when(mockWidgetRegistrar.getWidgetClass(eq(WidgetConstants.YOUTUBE_CONTENT_TYPE))).thenReturn(YouTubeWidgetDescriptor.class.getName());
		when(mockWidgetRegistrar.getFriendlyTypeName(eq(WidgetConstants.YOUTUBE_CONTENT_TYPE))).thenReturn(WidgetConstants.YOUTUBE_FRIENDLY_NAME);
		when(mockNodeModelCreator.newInstance(eq(YouTubeWidgetDescriptor.class.getName()))).thenReturn(descriptor1);
		when(mockNodeModelCreator.createJSONEntity(anyString(), anyString())).thenReturn(descriptor1);
		when(mockJSONObjectAdapter.createNew()).thenReturn(new JSONObjectAdapterImpl());
		when(mockView.getName()).thenReturn(attachment1Name);
	}

	@Test
	public void testEditNew() {
		presenter.editNew(entity.getId(), WidgetConstants.YOUTUBE_CONTENT_TYPE, entity.getAttachments());
		verify(mockView).setWidgetDescriptor(eq(entity.getId()), eq(WidgetConstants.YOUTUBE_CONTENT_TYPE), eq(descriptor1));
		verify(mockView).setName(eq(WidgetConstants.YOUTUBE_FRIENDLY_NAME));
		verify(mockView).show(eq(WidgetConstants.YOUTUBE_FRIENDLY_NAME));
	}
	@Test (expected=IllegalArgumentException.class)
	public void testEditNewFailedPreconditions1() {
		presenter.editNew(null, WidgetConstants.YOUTUBE_CONTENT_TYPE, entity.getAttachments());
	}
	@Test (expected=IllegalArgumentException.class)
	public void testEditNewFailedPreconditions2() {
		presenter.editNew(entity.getId(), null, entity.getAttachments());
	}
	
	@Test
	public void testEditExisting() {
		presenter.editExisting(entity.getId(), attachment1.getName(), entity.getAttachments());
		verify(mockView).setWidgetDescriptor(eq(entity.getId()), eq(WidgetConstants.YOUTUBE_CONTENT_TYPE), eq(descriptor1));
		verify(mockView).setName(eq(attachment1.getName()));
		verify(mockView).show(eq(WidgetConstants.YOUTUBE_FRIENDLY_NAME));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testEditExistingFailedPreconditions1() {
		presenter.editExisting(null, attachment1.getName(), entity.getAttachments());
	}
	@Test (expected=IllegalArgumentException.class)
	public void testEditExistingFailedPreconditions2() {
		presenter.editExisting(entity.getId(), null, entity.getAttachments());
	}
	
	@Test
	public void testApplySimpleInsert() throws Exception {
		//in this case, the descriptor is telling us that we should simply insert some text into the description, and nothing more (examples are external images, and links)
		String myInsertText = "[](some markdown)";
		when(mockView.getTextToInsert(anyString())).thenReturn(myInsertText);
		presenter.apply();
		verify(mockView).hide();
		//verify it doesn't update the descriptor, or update the entity attachments in this case (it's a simple insert)
		verify(mockView, Mockito.times(0)).updateDescriptorFromView();
		verify(mockSynapse, Mockito.times(0)).addWidgetDescriptorToEntity(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testApply() throws Exception {
		//in this case, the descriptor is telling us that we should simply insert some text into the description, and nothing more (examples are external images, and links)
		when(mockView.getTextToInsert(anyString())).thenReturn(null);
		//set widget by telling it to edit a new one
		presenter.editNew(entity.getId(), WidgetConstants.YOUTUBE_CONTENT_TYPE, entity.getAttachments());
		presenter.apply();
		//verify it updates the descriptor from the view, and updates the entity attachments
		verify(mockView).updateDescriptorFromView();
		verify(mockSynapse).addWidgetDescriptorToEntity(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}
}