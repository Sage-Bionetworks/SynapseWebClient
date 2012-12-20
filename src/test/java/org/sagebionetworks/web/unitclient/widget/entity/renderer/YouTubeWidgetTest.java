package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidgetView;

public class YouTubeWidgetTest {
		
	YouTubeWidget widget;
	YouTubeWidgetView mockView;
	
	@Before
	public void setup(){
		mockView = mock(YouTubeWidgetView.class);
		widget = new YouTubeWidget(mockView);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		YouTubeWidgetDescriptor descriptor = new YouTubeWidgetDescriptor();
		String videoId = "my test video id";
		descriptor.setVideoId(videoId);
		widget.configure("", descriptor);
		verify(mockView).configure(anyString(), eq(videoId));
	}
}
