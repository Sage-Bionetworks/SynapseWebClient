package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.sagebionetworks.web.client.DisplayUtils;

public class DisplayUtilsTest {
	
	@Test
	public void testGetMimeType(){
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("test.tar.gz", "gz");
		expected.put("test.txt", "txt");
		expected.put("test", null);
		expected.put("test.", null);
		for(String fileName: expected.keySet()){
			String expectedMime = expected.get(fileName);
			String mime = DisplayUtils.getMimeType(fileName);
			assertEquals(expectedMime, mime);
		}
	}
	
	@Test
	public void testGetIcon(){
		Map<String, String> expected = new HashMap<String, String>();
		String compressed = 
		expected.put("test.tar.GZ", DisplayUtils.DEFAULT_COMPRESSED_ICON);
		expected.put("test.doc", DisplayUtils.DEFAULT_TEXT_ICON);
		expected.put("test", DisplayUtils.UNKNOWN_ICON);
		expected.put("test.", DisplayUtils.UNKNOWN_ICON);
		expected.put("test.PDF", DisplayUtils.DEFAULT_PDF_ICON);
		expected.put("test.Zip", DisplayUtils.DEFAULT_COMPRESSED_ICON);
		expected.put("test.png", DisplayUtils.DEFAULT_IMAGE_ICON);
		for(String fileName: expected.keySet()){
			String expectedIcon = expected.get(fileName);
			String icon = DisplayUtils.getAttachmentIcon(fileName);
			assertEquals(expectedIcon, icon);
		}
	}
	
	@Test
	public void testFixWikiLinks(){
		String testHref = "Hello <a href=\"/wiki/HelloWorld.html\">World</a>";
		String expectedHref = "Hello <a href=\"https://sagebionetworks.jira.com/wiki/HelloWorld.html\">World</a>";
		String actualHref = DisplayUtils.fixWikiLinks(testHref);
		Assert.assertEquals(actualHref, expectedHref);
	}
	
	@Test
	public void testFixEmbeddedYouTube(){
		String testYouTube = "Hello video:<p> www.youtube.com/embed/xSfd5mkkmGM </p>";
		String expectedYouTube = "Hello video:<p> <iframe width=\"300\" height=\"169\" src=\"https://www.youtube.com/embed/xSfd5mkkmGM \" frameborder=\"0\" allowfullscreen=\"true\"></iframe></p>";
		String actualYouTube = DisplayUtils.fixEmbeddedYouTube(testYouTube);
		Assert.assertEquals(actualYouTube, expectedYouTube);
	}

	@Test
	public void testYouTubeVideoIdToUrl(){
		String testVideoId=  "xSfd5mkkmGM";
		String expectedUrl = "http://www.youtube.com/watch?v=xSfd5mkkmGM";
		String actualUrl = DisplayUtils.getYouTubeVideoUrl(testVideoId);
		Assert.assertEquals(actualUrl, expectedUrl);
	}
	
	@Test
	public void testYouTubeVideoIdFromUrl(){
		String testVideoUrl=  "http://www.youtube.com/watch?v=b1SJ7yaa7cI";
		String expectedId = "b1SJ7yaa7cI";
		String actualId = DisplayUtils.getYouTubeVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);
		
		testVideoUrl = "http://www.youtube.com/watch?v=aTestVideoId&feature=g-upl";
		expectedId = "aTestVideoId";
		actualId = DisplayUtils.getYouTubeVideoId(testVideoUrl);
		Assert.assertEquals(actualId, expectedId);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidYouTubeVideoIdFromUrl1(){
		String testVideoUrl=  "http://www.youtube.com/watch?v=";
		DisplayUtils.getYouTubeVideoId(testVideoUrl);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidYouTubeVideoIdFromUrl2(){
		String testVideoUrl=  "http://www.cnn.com/";
		DisplayUtils.getYouTubeVideoId(testVideoUrl);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidYouTubeVideoIdFromUrl3(){
		String testVideoUrl=  "";
		DisplayUtils.getYouTubeVideoId(testVideoUrl);
	}
	
	@Test
	public void testGetFileNameFromLocationPath() {
		String name = "filename.txt";
		assertEquals(name, DisplayUtils.getFileNameFromLocationPath("http://some.really.long.com/path/to/a/file/" + name));
		assertEquals(name, DisplayUtils.getFileNameFromLocationPath("http://some.really.long.com/path/to/a/file/" + name + "?param1=value&param2=value"));
		assertEquals(name, DisplayUtils.getFileNameFromLocationPath("/root/" + name));
		assertEquals(name, DisplayUtils.getFileNameFromLocationPath("http://google.com/" + name));
		
	}
	
}
