package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.editor.SynapseFormConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.SynapseFormConfigView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class SynapseFormConfigEditorTest {
	SynapseFormConfigEditor editor;
	@Mock
	SynapseFormConfigView mockView;
	@Mock
	EntityFinder mockEntityFinder;
	@Captor
	ArgumentCaptor<SelectedHandler> captor;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		editor = new SynapseFormConfigEditor(mockView, mockEntityFinder);
	}

	@Test
	public void testConstructorAndEntitySelection() {
		verify(mockView).setPresenter(editor);
		verify(mockView).initView();
		// verify entity finder is configured

		verify(mockEntityFinder).configure(eq(EntityFilter.PROJECT_OR_TABLE), eq(true), captor.capture());
		SelectedHandler selectedHandler = captor.getValue();
		Reference selected = new Reference();

		// invalid selection is handled by the entity finder
		String targetId = "syn314";
		selected.setTargetId(targetId);
		selectedHandler.onSelected(selected);
		verify(mockView).setEntityId(targetId);
		verify(mockEntityFinder).hide();
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
		descriptor.put(WidgetConstants.TABLE_ID_KEY, entityId);
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setEntityId(entityId);
	}

	@Test
	public void testUpdateDescriptorFromView() {
		String entityId = "syn123";
		when(mockView.getEntityId()).thenReturn(entityId);
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);

		editor.updateDescriptorFromView();
		verify(mockView).getEntityId();
		assertEquals(entityId, descriptor.get(WidgetConstants.TABLE_ID_KEY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateDescriptorFromViewInvalidSelection() {
		when(mockView.getEntityId()).thenReturn("");
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		editor.updateDescriptorFromView();
	}
}
