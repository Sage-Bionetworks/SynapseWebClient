package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class BaseEditWidgetDescriptorPresenterTest {

	BaseEditWidgetDescriptorPresenter presenter;
	BaseEditWidgetDescriptorView mockView;
	SynapseClientAsync mockSynapse;
	WidgetRegistrar mockWidgetRegistrar;
	ExampleEntity entity;
	Map<String, String> descriptor1;
	WidgetDescriptorUpdatedHandler mockDescriptorUpdatedHandler;
	ArgumentCaptor<WidgetDescriptorUpdatedEvent> descriptorUpdateEventCaptor;

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

		descriptor1 = new HashMap<String, String>();
		descriptor1.put(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY, "myVideoId");
		when(mockWidgetRegistrar.getFriendlyTypeName(eq(WidgetConstants.YOUTUBE_CONTENT_TYPE))).thenReturn(WidgetConstants.YOUTUBE_FRIENDLY_NAME);
		mockDescriptorUpdatedHandler = mock(WidgetDescriptorUpdatedHandler.class);
		presenter.addWidgetDescriptorUpdatedHandler(mockDescriptorUpdatedHandler);
		descriptorUpdateEventCaptor = ArgumentCaptor.forClass(WidgetDescriptorUpdatedEvent.class);
	}

	@Test
	public void testEditNew() {
		descriptor1.clear(); // should be no arguments passed to the view, since this is editing a new widget

		presenter.editNew(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), null), WidgetConstants.YOUTUBE_CONTENT_TYPE);
		verify(mockView).setWidgetDescriptor(any(WikiPageKey.class), eq(WidgetConstants.YOUTUBE_CONTENT_TYPE), eq(descriptor1));
		verify(mockView).show();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEditNewFailedPreconditions2() {
		presenter.editNew(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), null), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEditNewFailedPreconditions3() {
		presenter.editNew(new WikiPageKey(entity.getId(), null, null), WidgetConstants.YOUTUBE_CONTENT_TYPE);
	}

	@Test
	public void testApplySimpleInsertAndAddFileHandles() throws Exception {
		// in this case, the descriptor is telling us that we should simply insert some text into the
		// description, and nothing more (examples are external images, and links)
		String myInsertText = "[](some markdown)";
		when(mockView.getTextToInsert()).thenReturn(myInsertText);
		List<String> fileHandleIds = new ArrayList<String>();
		fileHandleIds.add("123");
		fileHandleIds.add("4");
		when(mockView.getNewFileHandleIds()).thenReturn(fileHandleIds);
		presenter.apply();
		verify(mockView).clearErrors();
		verify(mockView).hide();
		// it should always update the entity attachments
		verify(mockView).updateDescriptorFromView();
		verify(mockDescriptorUpdatedHandler).onUpdate(descriptorUpdateEventCaptor.capture());
		assertEquals(myInsertText, descriptorUpdateEventCaptor.getValue().getInsertValue());
		assertEquals(fileHandleIds, descriptorUpdateEventCaptor.getValue().getNewFileHandleIds());
	}

	@Test
	public void testApply() throws Exception {
		// in this case, the descriptor is telling us that we should simply insert some text into the
		// description, and nothing more (examples are external images, and links)
		when(mockView.getTextToInsert()).thenReturn(null);
		// set widget by telling it to edit a new one
		presenter.editNew(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), null), WidgetConstants.YOUTUBE_CONTENT_TYPE);
		presenter.apply();
		verify(mockView).clearErrors();
		// verify it updates the descriptor from the view, and updates the entity attachments
		verify(mockView).updateDescriptorFromView();
	}
}
