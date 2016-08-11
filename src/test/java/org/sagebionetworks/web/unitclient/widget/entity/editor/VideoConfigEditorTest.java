package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class VideoConfigEditorTest {
		
	VideoConfigEditor editor;
	VideoConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	EntityBundle mockBundle;
	@Mock
	Reference mockSelectedEntityReference;
	String selectedEntityId = "syn9347";
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		mockView = mock(VideoConfigView.class);
		editor = new VideoConfigEditor(mockView, mockSynapseClient);
		
		AsyncMockStubber.callSuccessWith(mockBundle).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		when(mockSelectedEntityReference.getTargetId()).thenReturn(selectedEntityId);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	
	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		String mp4VideoId = "syn123";
		String webMVideoId = "syn456";
		String oggVideoId = "syn789";
		descriptor.put(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY, mp4VideoId);
		descriptor.put(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY, webMVideoId);
		descriptor.put(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY, oggVideoId);
		
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setEntity(eq(mp4VideoId));
	}
	
	@Test
	public void testUpdateDescriptorFromView() {
		String mp4VideoId = "syn123";
		when(mockView.getEntity()).thenReturn(mp4VideoId);
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY, mp4VideoId);
		editor.configure(wikiKey, descriptor, null);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getEntity();
		//verify descriptor has the expected values
		assertEquals(mp4VideoId, descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY));
	}
	

	@Test
	public void testValidateSelectionMP4() {
		String fileName = "video.Mp4";
		when(mockBundle.getFileName()).thenReturn(fileName);
		
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		editor.validateSelection(mockSelectedEntityReference);
		
		verify(mockSynapseClient).getEntityBundle(eq(selectedEntityId), anyInt(), any(AsyncCallback.class));
		verify(mockView, never()).setVideoFormatWarningVisible(true);
		verify(mockView).setEntity(selectedEntityId);
		verify(mockView).hideFinder();
		
		when(mockView.getEntity()).thenReturn(selectedEntityId);
		editor.updateDescriptorFromView();
		assertEquals(selectedEntityId, descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY));
	}
	
	@Test
	public void testValidateSelectionOgg() {
		String fileName = "video.Ogg";
		when(mockBundle.getFileName()).thenReturn(fileName);
		
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		editor.validateSelection(mockSelectedEntityReference);
		
		verify(mockSynapseClient).getEntityBundle(eq(selectedEntityId), anyInt(), any(AsyncCallback.class));
		verify(mockView).setVideoFormatWarningVisible(true);
		verify(mockView).setEntity(selectedEntityId);
		verify(mockView).hideFinder();
		
		when(mockView.getEntity()).thenReturn(selectedEntityId);
		editor.updateDescriptorFromView();
		assertEquals(selectedEntityId, descriptor.get(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY));
	}
	
	@Test
	public void testValidateSelectionWebm() {
		String fileName = "video.WebM";
		when(mockBundle.getFileName()).thenReturn(fileName);
		
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		editor.validateSelection(mockSelectedEntityReference);
		
		verify(mockSynapseClient).getEntityBundle(eq(selectedEntityId), anyInt(), any(AsyncCallback.class));
		verify(mockView).setVideoFormatWarningVisible(true);
		verify(mockView).setEntity(selectedEntityId);
		verify(mockView).hideFinder();
		
		when(mockView.getEntity()).thenReturn(selectedEntityId);
		editor.updateDescriptorFromView();
		assertEquals(selectedEntityId, descriptor.get(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY));
	}
	
	@Test
	public void testValidateSelectionInvalidType() {
		String fileName = "video.not.recognized";
		when(mockBundle.getFileName()).thenReturn(fileName);
		
		editor.validateSelection(mockSelectedEntityReference);
		
		verify(mockSynapseClient).getEntityBundle(eq(selectedEntityId), anyInt(), any(AsyncCallback.class));
		verify(mockView).setVideoFormatWarningVisible(true);
		verify(mockView).showFinderError(VideoConfigEditor.UNRECOGNIZED_VIDEO_FORMAT_MESSAGE);
		verify(mockView, never()).setEntity(selectedEntityId);
		verify(mockView, never()).hideFinder();
	}
	
	@Test
	public void testValidateSelectionRPCError() {
		Exception ex = new Exception("error seeking file name");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		editor.validateSelection(mockSelectedEntityReference);
		
		verify(mockSynapseClient).getEntityBundle(eq(selectedEntityId), anyInt(), any(AsyncCallback.class));
		verify(mockView).showFinderError(ex.getMessage());
	}
	
	

	@Test
	public void testRecognizedFiletype() {
		assertTrue(VideoConfigEditor.isRecognizedMP4FileName("video.mp4"));
		assertTrue(VideoConfigEditor.isRecognizedMP4FileName("video.1.m4a"));
		assertTrue(VideoConfigEditor.isRecognizedMP4FileName("video.m4R"));
		assertTrue(VideoConfigEditor.isRecognizedMP4FileName("video.2.M4V"));
		assertTrue(VideoConfigEditor.isRecognizedMP4FileName("video.m4R"));
		assertFalse(VideoConfigEditor.isRecognizedMP4FileName("video.m44"));
		assertFalse(VideoConfigEditor.isRecognizedMP4FileName("video.OGG"));
		
		assertTrue(VideoConfigEditor.isRecognizedOggFileName("video.1.ogg"));
		assertTrue(VideoConfigEditor.isRecognizedOggFileName("video.OGV"));
		assertFalse(VideoConfigEditor.isRecognizedOggFileName("video.webm"));
		assertFalse(VideoConfigEditor.isRecognizedOggFileName("video.mp4"));
		
		assertTrue(VideoConfigEditor.isRecognizedWebMFileName("video.1.webm"));
		assertFalse(VideoConfigEditor.isRecognizedWebMFileName("video.1.mp4"));
		assertFalse(VideoConfigEditor.isRecognizedWebMFileName("video.1.ogg"));
	}
}
