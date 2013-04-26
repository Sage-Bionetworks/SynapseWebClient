package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
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
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WikiPageKey;

public class BaseEditWidgetDescriptorPresenterTest {
	
	BaseEditWidgetDescriptorPresenter presenter;
	BaseEditWidgetDescriptorView mockView;
	SynapseClientAsync mockSynapse;
	WidgetRegistrar mockWidgetRegistrar;
	ExampleEntity entity;
	Map<String, String> descriptor1;
	AttachmentData attachment1, attachment2;
	
	@Before
	public void setup() throws Exception {
		mockView = mock(BaseEditWidgetDescriptorView.class);
		mockSynapse = mock(SynapseClientAsync.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		presenter = new BaseEditWidgetDescriptorPresenter(mockView, mockWidgetRegistrar);	
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
	}

	@Test
	public void testEditNew() {
		descriptor1.clear();  //should be no arguments passed to the view, since this is editing a new widget
		
		presenter.editNew(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), null), WidgetConstants.YOUTUBE_CONTENT_TYPE, true);
		verify(mockView).setWidgetDescriptor(any(WikiPageKey.class), eq(WidgetConstants.YOUTUBE_CONTENT_TYPE), eq(descriptor1), anyBoolean());
		verify(mockView).show(eq(WidgetConstants.YOUTUBE_FRIENDLY_NAME));
	}
	@Test (expected=IllegalArgumentException.class)
	public void testEditNewFailedPreconditions1() {
		presenter.editNew(null,WidgetConstants.YOUTUBE_CONTENT_TYPE, true);
	}
	@Test (expected=IllegalArgumentException.class)
	public void testEditNewFailedPreconditions2() {
		presenter.editNew(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), null), null, true);
	}
	@Test (expected=IllegalArgumentException.class)
	public void testEditNewFailedPreconditions3() {
		presenter.editNew(new WikiPageKey(entity.getId(), null, null), WidgetConstants.YOUTUBE_CONTENT_TYPE, true);
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
		presenter.editNew(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), null), WidgetConstants.YOUTUBE_CONTENT_TYPE, true);
		presenter.apply();
		//verify it updates the descriptor from the view, and updates the entity attachments
		verify(mockView).updateDescriptorFromView();
	}
}