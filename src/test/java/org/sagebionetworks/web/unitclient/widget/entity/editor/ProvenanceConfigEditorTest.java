package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

public class ProvenanceConfigEditorTest {
		
	ProvenanceConfigEditor editor;
	ProvenanceConfigView mockView;
	
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
		descriptor.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, d);
		descriptor.put(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY, entityToGraph);
		descriptor.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, showExpand);
		editor.configure("","", descriptor);
		verify(mockView).setEntityId(eq(entityToGraph));
		verify(mockView).setIsExpanded(eq(Boolean.valueOf(showExpand)));
		verify(mockView).setDepth(eq(Long.parseLong(d)));
		
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		//verify(mockView).getDepth();
		verify(mockView).getEntityId();
		//verify(mockView).isExpanded();
	}
}
