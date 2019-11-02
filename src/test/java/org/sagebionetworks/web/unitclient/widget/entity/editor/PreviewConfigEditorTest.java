package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.editor.PreviewConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.PreviewConfigView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class PreviewConfigEditorTest {

	PreviewConfigEditor editor;
	PreviewConfigView mockView;
	EntityFinder mockEntityFinder;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);

	@Before
	public void setup() {
		mockView = mock(PreviewConfigView.class);
		mockEntityFinder = mock(EntityFinder.class);
		editor = new PreviewConfigEditor(mockView, mockEntityFinder);
	}


	@Test
	public void testConstructorAndEntitySelection() {
		verify(mockView).setPresenter(editor);
		verify(mockView).initView();
		// verify entity finder is configured
		ArgumentCaptor<SelectedHandler> captor = ArgumentCaptor.forClass(SelectedHandler.class);
		verify(mockEntityFinder).configure(eq(EntityFilter.ALL_BUT_LINK), eq(true), captor.capture());
		SelectedHandler selectedHandler = captor.getValue();
		Reference selected = new Reference();

		// invalid selection is handled by the entity finder
		String targetId = "syn314";
		selected.setTargetId(targetId);
		selectedHandler.onSelected(selected);
		verify(mockView).setVersion("");
		verify(mockView).setEntityId(targetId);
		verify(mockEntityFinder).hide();

		// invoke valid selection with version
		reset(mockView);
		Long version = 55L;
		selected.setTargetVersionNumber(version);
		selectedHandler.onSelected(selected);
		verify(mockView).setVersion(version.toString());
		verify(mockView).setEntityId(targetId);
	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}


	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		String entityId = "syn123";
		String version = "33";
		descriptor.put(WidgetConstants.WIDGET_ENTITY_ID_KEY, entityId);
		descriptor.put(WidgetConstants.WIDGET_ENTITY_VERSION_KEY, version);
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setEntityId(entityId);
		verify(mockView).setVersion(version);
	}


	@Test
	public void testUpdateDescriptorFromView() {
		String entityId = "syn123";
		String version = "88";

		when(mockView.getEntityId()).thenReturn(entityId);
		when(mockView.getVersion()).thenReturn(version);

		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);

		editor.updateDescriptorFromView();
		verify(mockView).getEntityId();
		verify(mockView).getVersion();

		// verify descriptor has the expected values
		assertEquals(entityId, descriptor.get(WidgetConstants.WIDGET_ENTITY_ID_KEY));
		assertEquals(version, descriptor.get(WidgetConstants.WIDGET_ENTITY_VERSION_KEY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateDescriptorFromViewInvalidSelection() {
		String version = "88";
		when(mockView.getEntityId()).thenReturn("");
		when(mockView.getVersion()).thenReturn(version);
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		editor.updateDescriptorFromView();
	}

	@Test(expected = NumberFormatException.class)
	public void testUpdateDescriptorFromViewInvalidVersion() {
		String entityId = "syn123";
		String version = "foo";
		when(mockView.getEntityId()).thenReturn(entityId);
		when(mockView.getVersion()).thenReturn(version);
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		editor.updateDescriptorFromView();
	}
}
