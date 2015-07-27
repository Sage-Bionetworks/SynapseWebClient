package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.widget.entity.editor.IFrameConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;
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
		verify(mockView).setVideoUrl("http://www.youtube.com/watch?v=" + videoId);
		
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getVideoUrl();
	}
	
	@Test
	public void testYouTubeVideoIdFromUrl(){
		String testVideoUrl=  "http://www.youtube.com/watch?v=b1SJ7yaa7cI";
		String expectedId = "b1SJ7yaa7cI";
		String actualId = editor.getYouTubeVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);
		
		testVideoUrl = "http://www.youtube.com/watch?v=aTestVideoId&feature=g-upl";
		expectedId = "aTestVideoId";
		actualId = editor.getYouTubeVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidYouTubeVideoIdFromUrl1(){
		String testVideoUrl = "http://www.youtube.com/watch?v=";
		editor.getYouTubeVideoId(testVideoUrl);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidYouTubeVideoIdFromUrl2(){
		String testVideoUrl = "http://www.cnn.com/";
		editor.getYouTubeVideoId(testVideoUrl);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidYouTubeVideoIdFromUrl3(){
		String testVideoUrl = "";
		editor.getYouTubeVideoId(testVideoUrl);
	}
	
}
