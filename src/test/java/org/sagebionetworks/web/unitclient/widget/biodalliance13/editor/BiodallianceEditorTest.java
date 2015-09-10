package org.sagebionetworks.web.unitclient.widget.biodalliance13.editor;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
	String chr = "99";
	Species species = Species.MOUSE;
	String viewStart = "4444";
	String viewEnd = "55555";

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
		
		//by default, view has valid values
		String chr = "99";
		String viewStart = "4444";
		String viewEnd = "55555";
		when(mockView.getChr()).thenReturn(chr);
		when(mockView.getViewStart()).thenReturn(viewStart);
		when(mockView.getViewEnd()).thenReturn(viewEnd);
		when(mockView.isMouse()).thenReturn(true);
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

	public void testValidCheckParams() {
		editor.checkParams();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidChr1() {
		when(mockView.getChr()).thenReturn("");
		editor.checkParams();
	}
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidChr2() {
		when(mockView.getChr()).thenReturn("a");
		editor.checkParams();
	}
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidChr3() {
		when(mockView.getChr()).thenReturn("-1");
		editor.checkParams();
	}

	@Test (expected=IllegalArgumentException.class)
	public void testInvalidCViewStart() {
		when(mockView.getViewStart()).thenReturn("");
		editor.checkParams();
	}
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidCViewStart2() {
		when(mockView.getViewStart()).thenReturn("b");
		editor.checkParams();
	}
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidCViewStart3() {
		when(mockView.getViewStart()).thenReturn("-4");
		editor.checkParams();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidCViewEnd() {
		when(mockView.getViewEnd()).thenReturn("");
		editor.checkParams();
	}
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidCViewEnd2() {
		when(mockView.getViewEnd()).thenReturn("c");
		editor.checkParams();
	}
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidCViewEnd3() {
		when(mockView.getViewEnd()).thenReturn("-5");
		editor.checkParams();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidCViewStartEnd() {
		//start can't be greater than end
		when(mockView.getViewStart()).thenReturn("10");
		when(mockView.getViewEnd()).thenReturn("5");
		editor.checkParams();
	}

	private BiodallianceSourceEditor setupTrackEditor() {
		BiodallianceSourceEditor mockSourceEditor= mock(BiodallianceSourceEditor.class);
		JSONObject jsonObject = mock(JSONObject.class);
		when(mockSourceEditor.toJsonObject()).thenReturn(jsonObject);
		when(jsonObject.toString()).thenReturn(testSourceJsonString);
		return mockSourceEditor;
	}

	@Test
	public void testMoveAndDelete() {
		BiodallianceSourceEditor s1 = setupTrackEditor();
		BiodallianceSourceEditor s2 = setupTrackEditor();
		BiodallianceSourceEditor s3 = setupTrackEditor();
		
		when(mockGinInjector.getBiodallianceSourceEditor()).thenReturn(s1, s2, s3);
		//add the 3 tracks
		editor.addTrackClicked();
		editor.addTrackClicked();
		editor.addTrackClicked();
		
		//check source order with move up, delete, and down
		List<BiodallianceSourceEditor> sourceEditors = editor.getSourceEditors();
		assertEquals(Arrays.asList(s1, s2, s3), sourceEditors);
		
		editor.moveUp(s2);
		assertEquals(Arrays.asList(s2, s1, s3), sourceEditors);
		
		editor.delete(s3);
		assertEquals(Arrays.asList(s2, s1), sourceEditors);
		
		editor.moveDown(s2);
		assertEquals(Arrays.asList(s1, s2), sourceEditors);
	}

}
