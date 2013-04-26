package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ProvenanceConfigEditorTest {
		
	ProvenanceConfigEditor editor;
	ProvenanceConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
	@Before
	public void setup(){
		mockView = mock(ProvenanceConfigView.class);
		editor = new ProvenanceConfigEditor(mockView);
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
		editor.configure(wikiKey, descriptor);
		verify(mockView).setEntityList(eq(entityToGraph));
		verify(mockView).setIsExpanded(eq(Boolean.valueOf(showExpand)));
		verify(mockView).setDepth(eq(Long.parseLong(d)));
		verify(mockView).setProvDisplayHeight(Integer.parseInt(displayHeight));
		
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getDepth();
		verify(mockView).getEntityList();
		verify(mockView).isExpanded();
		verify(mockView, atLeastOnce()).getProvDisplayHeight();
	}
}
