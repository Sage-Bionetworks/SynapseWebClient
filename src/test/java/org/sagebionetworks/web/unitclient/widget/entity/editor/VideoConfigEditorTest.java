package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigView;
import org.sagebionetworks.web.shared.WikiPageKey;

public class VideoConfigEditorTest {
		
	VideoConfigEditor editor;
	VideoConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
	@Before
	public void setup(){
		mockView = mock(VideoConfigView.class);
		editor = new VideoConfigEditor(mockView);
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
		verify(mockView).setMp4Entity(eq(mp4VideoId));
		verify(mockView).setWebMEntity(eq(webMVideoId));
		verify(mockView).setOggEntity(eq(oggVideoId));
		
	}
	
	public void testUpdateDescriptorFromView() {
		String mp4VideoId = "syn123";
		String webMVideoId = "syn456";
		String oggVideoId = "syn789";

		when(mockView.getMp4Entity()).thenReturn(mp4VideoId);
		when(mockView.getWebMEntity()).thenReturn(webMVideoId);
		when(mockView.getOggEntity()).thenReturn(oggVideoId);

		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getMp4Entity();
		verify(mockView).getWebMEntity();
		verify(mockView).getOggEntity();

		//verify descriptor has the expected values
		assertEquals(mp4VideoId, descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY));
		assertEquals(webMVideoId, descriptor.get(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY));
		assertEquals(oggVideoId, descriptor.get(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY));
	}
}
