package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;

public class BaseEditWidgetDescriptorPresenterTest {
	
	BaseEditWidgetDescriptorPresenter presenter;
	BaseEditWidgetDescriptorView mockView;
	SynapseClientAsync mockSynapse;
	NodeModelCreator mockNodeModelCreator;
	WidgetRegistrar mockWidgetRegistrar;
	JSONObjectAdapter mockJSONObjectAdapter;
	ExampleEntity entity;
	Map<String, String> descriptor1;
	AttachmentData attachment1, attachment2;
	
	@Before
	public void setup() throws Exception {
		mockView = mock(BaseEditWidgetDescriptorView.class);
		mockSynapse = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		mockJSONObjectAdapter = mock(JSONObjectAdapter.class);
		presenter = new BaseEditWidgetDescriptorPresenter(mockView, mockNodeModelCreator, mockJSONObjectAdapter, mockWidgetRegistrar);	
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
		
		descriptor1 = new HashMap<String, String>();
		descriptor1.put(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY, "myVideoId");
		when(mockWidgetRegistrar.getFriendlyTypeName(eq(WidgetConstants.YOUTUBE_CONTENT_TYPE))).thenReturn(WidgetConstants.YOUTUBE_FRIENDLY_NAME);
		when(mockJSONObjectAdapter.createNew()).thenReturn(new JSONObjectAdapterImpl());
	}

	@Test
	public void testEditNew() {
		descriptor1.clear();  //should be no arguments passed to the view, since this is editing a new widget
		presenter.editNew(entity.getId(),WidgetConstants.WIKI_OWNER_ID_ENTITY, WidgetConstants.YOUTUBE_CONTENT_TYPE);
		verify(mockView).setWidgetDescriptor(eq(entity.getId()), eq(WidgetConstants.WIKI_OWNER_ID_ENTITY), eq(WidgetConstants.YOUTUBE_CONTENT_TYPE), eq(descriptor1));
		verify(mockView).show(eq(WidgetConstants.YOUTUBE_FRIENDLY_NAME));
	}
	@Test (expected=IllegalArgumentException.class)
	public void testEditNewFailedPreconditions1() {
		presenter.editNew(null,WidgetConstants.WIKI_OWNER_ID_ENTITY, WidgetConstants.YOUTUBE_CONTENT_TYPE);
	}
	@Test (expected=IllegalArgumentException.class)
	public void testEditNewFailedPreconditions2() {
		presenter.editNew(entity.getId(),WidgetConstants.WIKI_OWNER_ID_ENTITY, null);
	}
	@Test (expected=IllegalArgumentException.class)
	public void testEditNewFailedPreconditions3() {
		presenter.editNew(entity.getId(),null, WidgetConstants.YOUTUBE_CONTENT_TYPE);
	}

	
	@Test
	public void testApplySimpleInsert() throws Exception {
		//in this case, the descriptor is telling us that we should simply insert some text into the description, and nothing more (examples are external images, and links)
		String myInsertText = "[](some markdown)";
		when(mockView.getTextToInsert()).thenReturn(myInsertText);
		presenter.apply();
		verify(mockView).hide();
		//it should always update the entity attachments
		verify(mockView).updateDescriptorFromView();
	}
	
	@Test
	public void testApply() throws Exception {
		//in this case, the descriptor is telling us that we should simply insert some text into the description, and nothing more (examples are external images, and links)
		when(mockView.getTextToInsert()).thenReturn(null);
		//set widget by telling it to edit a new one
		presenter.editNew(entity.getId(),WidgetConstants.WIKI_OWNER_ID_ENTITY, WidgetConstants.YOUTUBE_CONTENT_TYPE);
		presenter.apply();
		//verify it updates the descriptor from the view, and updates the entity attachments
		verify(mockView).updateDescriptorFromView();
	}
}