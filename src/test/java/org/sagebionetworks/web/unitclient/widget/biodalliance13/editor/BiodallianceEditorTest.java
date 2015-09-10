package org.sagebionetworks.web.unitclient.widget.biodalliance13.editor;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget.Species;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceEditor;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceEditorView;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditor;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.json.client.JSONObject;

public class BiodallianceEditorTest {
	HashMap<String, String> descriptor;
	BiodallianceEditor editor;
	BiodallianceEditorView mockView;
	PortalGinInjector mockGinInjector;
	BiodallianceSourceEditor mockSourceEditor;
	String testSourceJsonString = "json for source";
	@Before
	public void setup() throws Exception {
		mockView = mock(BiodallianceEditorView.class);
		mockGinInjector = mock(PortalGinInjector.class);
		descriptor = new HashMap<String, String>();
		editor = new BiodallianceEditor(mockView, mockGinInjector);
		mockSourceEditor= mock(BiodallianceSourceEditor.class);
		when(mockGinInjector.getBiodallianceSourceEditor()).thenReturn(mockSourceEditor);
		JSONObject jsonObject = mock(JSONObject.class);
		when(mockSourceEditor.toJsonObject()).thenReturn(jsonObject);
		when(jsonObject.toString()).thenReturn(testSourceJsonString);
	}
		
	@Test
	public void testConfigure() {
		//empty params
		editor.configure(null, descriptor, null);
		
		//verify defaults
		verify(mockView).setChr(BiodallianceWidget.DEFAULT_CHR);
		verify(mockView).setViewStart(Integer.toString(BiodallianceWidget.DEFAULT_VIEW_START));
		verify(mockView).setViewEnd(Integer.toString(BiodallianceWidget.DEFAULT_VIEW_END));
		//default to human
		verify(mockView).setHuman();
		verify(mockView).clearTracks();
		
	}
	
	@Test
	public void testConfigureNonDefaults() {
		String chr = "99";
		Species species = Species.MOUSE;
		String viewStart = "4444";
		String viewEnd = "55555";
		//add a couple of bogus sources
		descriptor.put(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + 0, "source1");
		descriptor.put(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + 1, "source2");
		
		descriptor.put(WidgetConstants.BIODALLIANCE_VIEW_START_KEY, viewStart);
		descriptor.put(WidgetConstants.BIODALLIANCE_VIEW_END_KEY, viewEnd);
		descriptor.put(WidgetConstants.BIODALLIANCE_CHR_KEY, chr);
		descriptor.put(WidgetConstants.BIODALLIANCE_SPECIES_KEY, species.name());
		
		editor.configure(null, descriptor, null);
		
		//verify values
		verify(mockView).setChr(chr);
		verify(mockView).setViewStart(viewStart);
		verify(mockView).setViewEnd(viewEnd);
		verify(mockView).setMouse();
		verify(mockView).clearTracks();
		
		verify(mockGinInjector, times(2)).getBiodallianceSourceEditor();
		verify(mockSourceEditor, times(2)).setSourceActionHandler(editor);
		verify(mockSourceEditor, times(2)).setSourceJson(anyString());
	}

	@Test
	public void testAddTrackClicked() {
		editor.addTrackClicked();
		verify(mockGinInjector).getBiodallianceSourceEditor();
		verify(mockSourceEditor).setSourceActionHandler(editor);
	}

	@Test
	public void testUpdateFromView() {
		editor.configure(null, descriptor, null);
		
		String chr = "99";
		
		String viewStart = "4444";
		String viewEnd = "55555";
		when(mockView.getChr()).thenReturn(chr);
		when(mockView.getViewStart()).thenReturn(viewStart);
		when(mockView.getViewEnd()).thenReturn(viewEnd);
		when(mockView.isMouse()).thenReturn(true);
		//add a track
		editor.addTrackClicked();
		
		editor.updateDescriptorFromView();

		//verify descriptor
		descriptor.get(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + 0);
		
		assertEquals(viewStart, descriptor.get(WidgetConstants.BIODALLIANCE_VIEW_START_KEY));
		assertEquals(viewEnd, descriptor.get(WidgetConstants.BIODALLIANCE_VIEW_END_KEY));
		assertEquals(chr, descriptor.get(WidgetConstants.BIODALLIANCE_CHR_KEY));
		assertEquals(Species.MOUSE.name(), descriptor.get(WidgetConstants.BIODALLIANCE_SPECIES_KEY));
	}

	@Test
	public void testCheckParams() {
		fail("Not yet implemented");
	}

	@Test
	public void testMoveUp() {
		fail("Not yet implemented");
	}

	@Test
	public void testMoveDown() {
		fail("Not yet implemented");
	}

	@Test
	public void testDelete() {
		fail("Not yet implemented");
	}

}
