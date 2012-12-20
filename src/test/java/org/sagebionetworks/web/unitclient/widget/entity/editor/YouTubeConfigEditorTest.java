package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigView;

public class YouTubeConfigEditorTest {
		
	YouTubeConfigEditor editor;
	YouTubeConfigView mockView;
	
	@Before
	public void setup(){
		mockView = mock(YouTubeConfigView.class);
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
		YouTubeWidgetDescriptor descriptor = new YouTubeWidgetDescriptor();
		String videoId = "my test video id";
		descriptor.setVideoId(videoId);
		editor.configure("", descriptor);
		verify(mockView).setVideoUrl(anyString());
		
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getVideoUrl();
	}
}
