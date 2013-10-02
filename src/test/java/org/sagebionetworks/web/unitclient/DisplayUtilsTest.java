package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.WidgetSelectionState;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

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
		assertEquals(name, DisplayUtils.getFileNameFromExternalUrl("http://some.really.long.com/path/to/a/file/" + name));
		assertEquals(name, DisplayUtils.getFileNameFromExternalUrl("http://some.really.long.com/path/to/a/file/" + name + "?param1=value&param2=value"));
		assertEquals(name, DisplayUtils.getFileNameFromExternalUrl("/root/" + name));
		assertEquals(name, DisplayUtils.getFileNameFromExternalUrl("http://google.com/" + name));
		
	}
	
	@Test
	public void testWidgetNotSelectedState() {
		WidgetSelectionState state = new WidgetSelectionState();
		String markdownText = "This contains no synapse widget";
		for (int i = 0; i < markdownText.length(); i++) {
			DisplayUtils.updateWidgetSelectionState(state, markdownText, i);
			assertFalse(state.isWidgetSelected());	
		}
		
		String synapseWidgetInnerText = "widget?param1=123&param2=456";
		String fullSynapseWidgetText = WidgetConstants.WIDGET_START_MARKDOWN + synapseWidgetInnerText + WidgetConstants.WIDGET_END_MARKDOWN;
		
		//verify that when selecting outside of the widget text, it will report that widget is not selected
		DisplayUtils.updateWidgetSelectionState(state, fullSynapseWidgetText + markdownText, fullSynapseWidgetText.length() + 1);
		assertFalse(state.isWidgetSelected());
		
		DisplayUtils.updateWidgetSelectionState(state, markdownText + fullSynapseWidgetText, 5);
		assertFalse(state.isWidgetSelected());
	}
	
	@Test
	public void testWidgetSelectedState() {
		WidgetSelectionState state = new WidgetSelectionState();
		String synapseWidgetInnerText = "widget?param1=123&param2=456";
		String fullSynapseWidgetText = WidgetConstants.WIDGET_START_MARKDOWN + synapseWidgetInnerText + WidgetConstants.WIDGET_END_MARKDOWN;
		String markdownText = " this contains a synapse widget somewhere ";
		
		//if the widget is at the beginning we should be able to find it when selecting inside, or the first character
		String testMarkdown = fullSynapseWidgetText + markdownText;
		DisplayUtils.updateWidgetSelectionState(state, testMarkdown, 0);
		assertTrue(state.isWidgetSelected());
		assertEquals(synapseWidgetInnerText, state.getInnerWidgetText());
		assertEquals(0, state.getWidgetStartIndex());
		assertEquals(fullSynapseWidgetText.length(), state.getWidgetEndIndex());
		
		DisplayUtils.updateWidgetSelectionState(state, testMarkdown, 5);
		assertTrue(state.isWidgetSelected());
		assertEquals(synapseWidgetInnerText, state.getInnerWidgetText());
		assertEquals(0, state.getWidgetStartIndex());
		assertEquals(fullSynapseWidgetText.length(), state.getWidgetEndIndex());
		
		
		//if the widget is at the end we should be able to find it when selecting inside, or the last character
		testMarkdown = markdownText + fullSynapseWidgetText;
		DisplayUtils.updateWidgetSelectionState(state, testMarkdown, testMarkdown.length()-1);
		assertTrue(state.isWidgetSelected());
		assertEquals(synapseWidgetInnerText, state.getInnerWidgetText());
		assertEquals(testMarkdown.length()-fullSynapseWidgetText.length(), state.getWidgetStartIndex());
		assertEquals(testMarkdown.length(), state.getWidgetEndIndex());
		
		//select inside, but this time let's add spaces inside one of the parameters (and continue to verify that it finds the entire widget text)
		synapseWidgetInnerText += "&param3=7 8 9";
		fullSynapseWidgetText = WidgetConstants.WIDGET_START_MARKDOWN + synapseWidgetInnerText + WidgetConstants.WIDGET_END_MARKDOWN;
		testMarkdown = markdownText + fullSynapseWidgetText;
		DisplayUtils.updateWidgetSelectionState(state, testMarkdown, testMarkdown.length()-fullSynapseWidgetText.length() + 4);
		assertTrue(state.isWidgetSelected());
		assertEquals(synapseWidgetInnerText, state.getInnerWidgetText());
		assertEquals(testMarkdown.length()-fullSynapseWidgetText.length(), state.getWidgetStartIndex());
		assertEquals(testMarkdown.length(), state.getWidgetEndIndex());
		
		//if the widget is in the middle we should be able to find it when selecting inside
		int insertPoint = 6;
		
		testMarkdown = markdownText.substring(0, insertPoint) + fullSynapseWidgetText + markdownText.substring(insertPoint);
		DisplayUtils.updateWidgetSelectionState(state, testMarkdown, insertPoint + 2);
		assertTrue(state.isWidgetSelected());
		assertEquals(synapseWidgetInnerText, state.getInnerWidgetText());
		assertEquals(insertPoint, state.getWidgetStartIndex());
		assertEquals(insertPoint + fullSynapseWidgetText.length(), state.getWidgetEndIndex());
	}
	
	@Test
	public void testInvalidWidget() {
		String markdownText = WidgetConstants.WIDGET_START_MARKDOWN + "onlyinvalid?because=it&does=not&end so \nwill the entire thing be overwritten?";
		WidgetSelectionState state = new WidgetSelectionState();
		//verify that when selecting outside of the widget text, it will report that widget is not selected
		DisplayUtils.updateWidgetSelectionState(state, markdownText, 2);
		assertFalse(state.isWidgetSelected());
	}
	
	@Test 
	public void testGetProjectId() {
		String projectId = "syn123";
		EntityPath path = new EntityPath();
		EntityHeader root = new EntityHeader();
		EntityHeader project = new EntityHeader();
		project.setId(projectId);
		project.setType(Project.class.getName());
		EntityHeader file = new EntityHeader();
		path.setPath(Arrays.asList(new EntityHeader[] { root, project, file }));
		assertEquals(project, DisplayUtils.getProjectHeader(path));
	}
}






