package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import junit.framework.Assert;

public class VideoConfigEditorTest {

	VideoConfigEditor editor;
	VideoConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	EntityBundle mockBundle;
	@Mock
	Reference mockSelectedEntityReference;
	String selectedEntityId = "syn9347";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockView = mock(VideoConfigView.class);
		editor = new VideoConfigEditor(mockView, mockSynapseJavascriptClient);

		AsyncMockStubber.callSuccessWith(mockBundle).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
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
		when(mockView.isSynapseEntity()).thenReturn(true);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getEntity();
		// verify descriptor has the expected values
		assertEquals(mp4VideoId, descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY));
	}


	@Test
	public void testValidateSelectionMP4() {
		String fileName = "video.Mp4";
		when(mockBundle.getFileName()).thenReturn(fileName);

		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		editor.validateSelection(mockSelectedEntityReference);

		verify(mockSynapseJavascriptClient).getEntityBundle(eq(selectedEntityId), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockView, never()).setVideoFormatWarningVisible(true);
		verify(mockView).setEntity(selectedEntityId);
		verify(mockView).hideFinder();

		when(mockView.getEntity()).thenReturn(selectedEntityId);
		when(mockView.isSynapseEntity()).thenReturn(true);
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

		verify(mockSynapseJavascriptClient).getEntityBundle(eq(selectedEntityId), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockView).setVideoFormatWarningVisible(true);
		verify(mockView).setEntity(selectedEntityId);
		verify(mockView).hideFinder();

		when(mockView.getEntity()).thenReturn(selectedEntityId);
		when(mockView.isSynapseEntity()).thenReturn(true);
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

		verify(mockSynapseJavascriptClient).getEntityBundle(eq(selectedEntityId), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockView).setVideoFormatWarningVisible(true);
		verify(mockView).setEntity(selectedEntityId);
		verify(mockView).hideFinder();

		when(mockView.getEntity()).thenReturn(selectedEntityId);
		when(mockView.isSynapseEntity()).thenReturn(true);
		editor.updateDescriptorFromView();
		assertEquals(selectedEntityId, descriptor.get(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY));
	}

	@Test
	public void testValidateSelectionInvalidType() {
		String fileName = "video.not.recognized";
		when(mockBundle.getFileName()).thenReturn(fileName);

		editor.validateSelection(mockSelectedEntityReference);

		verify(mockSynapseJavascriptClient).getEntityBundle(eq(selectedEntityId), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockView).setVideoFormatWarningVisible(true);
		verify(mockView).showFinderError(VideoConfigEditor.UNRECOGNIZED_VIDEO_FORMAT_MESSAGE);
		verify(mockView, never()).setEntity(selectedEntityId);
		verify(mockView, never()).hideFinder();
	}

	@Test
	public void testValidateSelectionRPCError() {
		Exception ex = new Exception("error seeking file name");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		editor.validateSelection(mockSelectedEntityReference);

		verify(mockSynapseJavascriptClient).getEntityBundle(eq(selectedEntityId), any(EntityBundleRequest.class), any(AsyncCallback.class));
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

		assertTrue(VideoConfigEditor.isRecognizedVideoFileName("video.mp4"));
		assertTrue(VideoConfigEditor.isRecognizedVideoFileName("video.1.m4a"));
		assertTrue(VideoConfigEditor.isRecognizedVideoFileName("video.m4R"));
		assertTrue(VideoConfigEditor.isRecognizedVideoFileName("video.2.M4V"));
		assertTrue(VideoConfigEditor.isRecognizedVideoFileName("video.m4R"));
		assertFalse(VideoConfigEditor.isRecognizedVideoFileName("video.m44"));
		assertTrue(VideoConfigEditor.isRecognizedVideoFileName("video.1.ogg"));
		assertTrue(VideoConfigEditor.isRecognizedVideoFileName("video.OGV"));
		assertTrue(VideoConfigEditor.isRecognizedVideoFileName("video.1.webm"));
	}

	@Test
	public void testConfigureVimeo() {
		when(mockView.isVimeoVideo()).thenReturn(true);
		when(mockView.getVimeoVideoUrl()).thenReturn("https://player.vimeo.com/video/9730308");
		Map<String, String> descriptor = new HashMap<String, String>();
		String videoId = "my test video id";
		descriptor.put(WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY, videoId);
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).showVimeoTab();
		verify(mockView).setVimeoVideoUrl(VideoConfigEditor.VIMEO_URL_PREFIX + videoId);

		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getVimeoVideoUrl();
	}

	@Test
	public void testVimeoIdFromUrl() {
		String testVideoUrl = "https://player.vimeo.com/video/9730308";
		String expectedId = "9730308";
		String actualId = editor.getVimeoVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);

		testVideoUrl = "https://player.vimeo.com/video/123123";
		expectedId = "123123";
		actualId = editor.getVimeoVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidVimeoIdFromUrl1() {
		String testVideoUrl = "https://player.vimeo.com/video/";
		editor.getVimeoVideoId(testVideoUrl);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidVimeoIdFromUrl2() {
		String testVideoUrl = "http://www.cnn.com/";
		editor.getVimeoVideoId(testVideoUrl);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidVimeoIdFromUrl3() {
		String testVideoUrl = "";
		editor.getVimeoVideoId(testVideoUrl);
	}

	@Test
	public void testConfigureYouTube() {
		when(mockView.getYouTubeVideoUrl()).thenReturn("http://www.youtube.com/watch?v=G0k3kHtyoqc");
		Map<String, String> descriptor = new HashMap<String, String>();
		String videoId = "my test video id";
		descriptor.put(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY, videoId);
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setYouTubeVideoUrl(VideoConfigEditor.YOUTUBE_URL_PREFIX + videoId);
		when(mockView.isYouTubeVideo()).thenReturn(true);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getYouTubeVideoUrl();
	}

	@Test
	public void testYouTubeVideoIdFromUrl() {
		String testVideoUrl = "http://www.youtube.com/watch?v=b1SJ7yaa7cI";
		String expectedId = "b1SJ7yaa7cI";
		String actualId = editor.getYouTubeVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);

		testVideoUrl = "http://www.youtube.com/watch?v=aTestVideoId&feature=g-upl";
		expectedId = "aTestVideoId";
		actualId = editor.getYouTubeVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidYouTubeVideoIdFromUrl1() {
		String testVideoUrl = "http://www.youtube.com/watch?v=";
		editor.getYouTubeVideoId(testVideoUrl);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidYouTubeVideoIdFromUrl2() {
		String testVideoUrl = "http://www.cnn.com/";
		editor.getYouTubeVideoId(testVideoUrl);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidYouTubeVideoIdFromUrl3() {
		String testVideoUrl = "";
		editor.getYouTubeVideoId(testVideoUrl);
	}
}
