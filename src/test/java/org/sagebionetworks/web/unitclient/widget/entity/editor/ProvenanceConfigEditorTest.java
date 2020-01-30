package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ProvenanceConfigEditorTest {

	ProvenanceConfigEditor editor;
	ProvenanceConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);

	@Before
	public void setup() {
		mockView = mock(ProvenanceConfigView.class);
		editor = new ProvenanceConfigEditor(mockView);
		when(mockView.getEntityList()).thenReturn("syn123");
		when(mockView.getDepth()).thenReturn("1");
		when(mockView.getProvDisplayHeight()).thenReturn("256");
	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		String d = "5";
		String entityToGraph = "syn1234555";
		String showExpand = "true";
		String displayHeight = "500";
		descriptor.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, d);
		descriptor.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, entityToGraph);
		descriptor.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, showExpand);
		descriptor.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, displayHeight);
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setEntityList(eq(entityToGraph));
		verify(mockView).setIsExpanded(eq(Boolean.valueOf(showExpand)));
		verify(mockView).setDepth(eq(d));
		verify(mockView).setProvDisplayHeight(displayHeight);

		editor.updateDescriptorFromView();
		verify(mockView).getDepth();
		verify(mockView).getEntityList();
		verify(mockView).isExpanded();
		verify(mockView, atLeastOnce()).getProvDisplayHeight();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateDescriptorFromViewInvalidDepth() {
		when(mockView.getDepth()).thenReturn("not a number");
		editor.updateDescriptorFromView();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateDescriptorFromViewInvalidDepth2() {
		when(mockView.getDepth()).thenReturn("22.3");
		editor.updateDescriptorFromView();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateDescriptorFromViewInvalidEntityList() {
		when(mockView.getEntityList()).thenReturn(null);
		editor.updateDescriptorFromView();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateDescriptorFromViewInvalidEntityList2() {
		when(mockView.getEntityList()).thenReturn("");
		editor.updateDescriptorFromView();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateDescriptorFromViewInvalidDisplayHeight() {
		when(mockView.getEntityList()).thenReturn(null);
		when(mockView.getDepth()).thenReturn("1");
		when(mockView.getProvDisplayHeight()).thenReturn("abc");
		editor.updateDescriptorFromView();
	}
}
