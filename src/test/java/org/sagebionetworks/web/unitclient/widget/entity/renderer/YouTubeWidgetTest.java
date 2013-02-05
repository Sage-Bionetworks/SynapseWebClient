package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
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
		Map<String, String> descriptor = new HashMap<String, String>();
		String videoId = "my test video id";
		descriptor.put(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY, videoId);
		widget.configure("", descriptor);
		verify(mockView).configure(anyString(), eq(videoId));
	}
}
