package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.widget.entity.WidgetSelectionState;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;

public class DisplayUtilsTest {
	
	private String textWithoutMarkdown = "This is the test markdown\nthat will be used.";
	private String textWithMarkdown = "This is the **test** markdown\nthat will be used.";
	private String markdownDelimiter = "*";
	private String markdownDelimiter2 = "**";
	private String errorMessage= "my test error message";
	private GlobalApplicationState mockGlobalApplicationState;
	private PlaceChanger mockPlaceChanger;
	private SynapseView mockView;
	
	@Before
	public void setup(){
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockView = mock(SynapseView.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}	

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
	
	@Test
	public void testGetDisplayName() {
		//DisplayUtils.getDisplayName(firstName, lastName, userName)
		String fName = "Strong";
		String lName = "Bad";
		String userName = "SBEmail";
		String tempUserName = WebConstants.TEMPORARY_USERNAME_PREFIX + "1234";
		
		//first, verify that the temp username is recognized as a temp username
		assertTrue(DisplayUtils.isTemporaryUsername(tempUserName));
		assertFalse(DisplayUtils.isTemporaryUsername(userName));
		
		//try combinations
		//display of an old user (first name and last name are set, but has a temp username)
		assertEquals("Strong Bad", DisplayUtils.getDisplayName(fName, lName, tempUserName));
		
		//possible new user state, where first and last names are not filled in during registration
		assertEquals("SBEmail", DisplayUtils.getDisplayName(null, null, userName));
		assertEquals("SBEmail", DisplayUtils.getDisplayName("", "", userName));
		
		//old user who has set the username
		assertEquals("Strong Bad (SBEmail)", DisplayUtils.getDisplayName(fName, lName, userName));
	}
	
	@Test
	public void testSurroundText() {
		//basic case, "test" selected, text should be surrounded with markdown
		int startPos = textWithoutMarkdown.indexOf("test");
		int selectionLength = "test".length();
		String result = DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
		assertEquals("This is the *test* markdown\nthat will be used.", result);
	}
	
	@Test
	public void testSurroundTextNoSelection() {
		//if no text is selected, then it should place the markdown around the current cursor position
		int startPos = textWithoutMarkdown.indexOf("test");
		int selectionLength = 0;
		String result = DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, true, startPos, selectionLength);
		assertEquals("This is the **test markdown\nthat will be used.", result);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testSurroundTextPastNewline() {
		int startPos = textWithoutMarkdown.indexOf("test");
		int selectionLength = "test markdown\nthat".length();
		DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
	}
		
	@Test
	public void testSurroundTextPastNewlineSupportMultiline() {
		int startPos = textWithoutMarkdown.indexOf("test");
		int selectionLength = "test markdown\nthat".length();
		DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, true, startPos, selectionLength);
	}
		
	@Test (expected=IllegalArgumentException.class)
	public void testSurroundTextPastEndOfLine() {
		int startPos = textWithoutMarkdown.indexOf("test");
		int selectionLength = "test markdown\nthat".length();
		DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
	}

	@Test (expected=IllegalArgumentException.class)
	public void testSurroundTextPastEndOfText() {
		//selection goes past end of string.
		int startPos = textWithoutMarkdown.indexOf("used");
		int selectionLength = 100;
		DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
	}

	@Test (expected=IllegalArgumentException.class)
	public void testSurroundTextInvalidStart() {
		int startPos = -1;
		int selectionLength = 2;
		DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testSurroundTextInvalidSelectionLength() {
		int startPos = 0;
		int selectionLength = -1;
		DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
	}
	
	@Test
	public void testSurroundTextStripMarkdownTest1() {
		int startPos = textWithMarkdown.indexOf("test");
		int selectionLength = "test".length();
		String result = DisplayUtils.surroundText(textWithMarkdown, markdownDelimiter2, markdownDelimiter2, false, startPos, selectionLength);
		assertEquals(textWithoutMarkdown, result);
	}

	@Test
	public void testSurroundTextStripMarkdownTest2() {
		//before the selection has the markdown, but after the selection does not
		int startPos = textWithMarkdown.indexOf("test");
		int selectionLength = "test".length() + 4;
		String result = DisplayUtils.surroundText(textWithMarkdown, markdownDelimiter2, markdownDelimiter2, false, startPos, selectionLength);
		assertEquals("This is the ****test** m**arkdown\nthat will be used.", result);
	}

	@Test
	public void testSurroundTextStripMarkdownTest3() {
		//after the selection has the markdown, but before the selection does not
		int startPos = textWithMarkdown.indexOf("test") - 6;
		int selectionLength = "test".length() + 6;
		String result = DisplayUtils.surroundText(textWithMarkdown, markdownDelimiter2, markdownDelimiter2, false, startPos, selectionLength);
		assertEquals("This is **the **test**** markdown\nthat will be used.", result);
	}
	
	@Test
	public void testSurroundTextStripMarkdownTest4() {
		//test end of text with markdown
		String textWithMarkdown = "end of the text has **markdown**";
		int startPos = textWithMarkdown.indexOf("markdown");
		int selectionLength = "markdown".length();
		String result = DisplayUtils.surroundText(textWithMarkdown, markdownDelimiter2, markdownDelimiter2, false, startPos, selectionLength);
		assertEquals("end of the text has markdown", result);
	}
	
	@Test
	public void testHeading() {
		String textWithoutMarkdown = "The new header is Section 1";
		int startPos = textWithoutMarkdown.indexOf("Section");
		int selectionLength = "Section 1".length();
		String result = DisplayUtils.surroundText(textWithoutMarkdown, "\n#", "", false, startPos, selectionLength);
		assertEquals("The new header is \n#Section 1", result);
}

	@Test
	public void testCodeBlock() {
		String text = "String text = \"foo\";\nString text2 = \"bar\"";
		int startPos = 0;
		int selectionLength = text.length();
		String markdownDelimiter = "\n```\n";
		String result = DisplayUtils.surroundText(text, markdownDelimiter, markdownDelimiter, true, startPos, selectionLength);
		assertEquals(markdownDelimiter+text+markdownDelimiter, result);
	}

	@Test
	public void testSurroundTextMarkdownEndOfText() {
		String textWithMarkdown = "end";
		int startPos = textWithMarkdown.length();
		int selectionLength = 0;
		String result = DisplayUtils.surroundText(textWithMarkdown, markdownDelimiter2, markdownDelimiter2, true, startPos, selectionLength);
		assertEquals(textWithMarkdown+markdownDelimiter2+markdownDelimiter2, result);
	}
	
	@Test
	public void testSurroundTextNoText() {
		int startPos = 0;
		int selectionLength = 0;
		String result = DisplayUtils.surroundText("", markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
		assertEquals(markdownDelimiter + markdownDelimiter, result);
	}
	
	@Test
	public void testHandleServiceExceptionReadOnly() {
		assertTrue(DisplayUtils.handleServiceException(new ReadOnlyModeException(), mockGlobalApplicationState, true, mockView));
		verify(mockView).showErrorMessage(eq(DisplayConstants.SYNAPSE_IN_READ_ONLY_MODE));
	}
	
	@Test
	public void testHandleServiceExceptionDown() {
		assertTrue(DisplayUtils.handleServiceException(new SynapseDownException(), mockGlobalApplicationState, true, mockView));
		verify(mockPlaceChanger).goTo(any(Down.class));
	}
	
	@Test
	public void testHandleServiceExceptionForbiddenLoggedIn() {
		assertTrue(DisplayUtils.handleServiceException(new ForbiddenException(), mockGlobalApplicationState, true, mockView));
		ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
		verify(mockView).showErrorMessage(c.capture());
		assertTrue(c.getValue().startsWith(DisplayConstants.ERROR_FAILURE_PRIVLEDGES));
	}
	
	@Test
	public void testHandleServiceExceptionForbiddenNotLoggedIn() {
		assertTrue(DisplayUtils.handleServiceException(new ForbiddenException(), mockGlobalApplicationState, false, mockView));
		verify(mockView).showErrorMessage(eq(DisplayConstants.ERROR_LOGIN_REQUIRED));
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}
	
	@Test
	public void testHandleServiceExceptionBadRequest() {
		assertTrue(DisplayUtils.handleServiceException(new BadRequestException(errorMessage), mockGlobalApplicationState, true, mockView));
		verify(mockView).showErrorMessage(eq(errorMessage));
	}

	@Test
	public void testHandleServiceExceptionNotFound() {
		assertTrue(DisplayUtils.handleServiceException(new NotFoundException(), mockGlobalApplicationState, true, mockView));
		verify(mockView).showErrorMessage(eq(DisplayConstants.ERROR_NOT_FOUND));
		verify(mockPlaceChanger).goTo(any(Home.class));
	}
	
	@Test
	public void testHandleServiceExceptionNotRecognized() {
		assertFalse(DisplayUtils.handleServiceException(new IllegalArgumentException(), mockGlobalApplicationState, true, mockView));
		Mockito.verifyZeroInteractions(mockView, mockPlaceChanger);
	}
	
	@Test
	public void testCreateEntityVersionString(){
		assertEquals("", DisplayUtils.createEntityVersionString(null, null));
		assertEquals("", DisplayUtils.createEntityVersionString("", null));
	}
}






