package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.IFrameConfigView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class YouTubeConfigEditorTest {
		
	YouTubeConfigEditor editor;
	IFrameConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
	@Before
	public void setup(){
		mockView = mock(IFrameConfigView.class);
		editor = new YouTubeConfigEditor(mockView);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		when(mockView.getVideoUrl()).thenReturn("http://www.youtube.com/watch?v=G0k3kHtyoqc");
		Map<String, String> descriptor = new HashMap<String, String>();
		String videoId = "my test video id";
		descriptor.put(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY, videoId);
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setVideoUrl(anyString());
		
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getVideoUrl();
	}
}
