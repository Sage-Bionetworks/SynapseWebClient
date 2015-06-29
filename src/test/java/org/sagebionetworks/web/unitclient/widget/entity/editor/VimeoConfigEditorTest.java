package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.anyString;
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
import org.sagebionetworks.web.client.widget.entity.editor.VimeoConfigEditor;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class VimeoConfigEditorTest {

	VimeoConfigEditor editor;
	IFrameConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
	@Before
	public void setup(){
		mockView = mock(IFrameConfigView.class);
		editor = new VimeoConfigEditor(mockView);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		when(mockView.getVideoUrl()).thenReturn("https://player.vimeo.com/video/9730308");
		Map<String, String> descriptor = new HashMap<String, String>();
		String videoId = "my test video id";
		descriptor.put(WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY, videoId);
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setVideoUrl("https://player.vimeo.com/video/" + videoId);
		
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getVideoUrl();
	}
	
	@Test
	public void testVimeoIdFromUrl(){
		String testVideoUrl = "https://player.vimeo.com/video/9730308";
		String expectedId = "9730308";
		String actualId = editor.getVimeoVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);
		
		testVideoUrl = "https://player.vimeo.com/video/123123";
		expectedId = "123123";
		actualId = editor.getVimeoVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidVimeoIdFromUrl1(){
		String testVideoUrl = "https://player.vimeo.com/video/";
		editor.getVimeoVideoId(testVideoUrl);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidVimeoIdFromUrl2(){
		String testVideoUrl = "http://www.cnn.com/";
		editor.getVimeoVideoId(testVideoUrl);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidVimeoIdFromUrl3(){
		String testVideoUrl = "";
		editor.getVimeoVideoId(testVideoUrl);
	}
	
}
