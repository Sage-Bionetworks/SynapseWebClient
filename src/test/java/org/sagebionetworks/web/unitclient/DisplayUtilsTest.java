package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sagebionetworks.web.client.DisplayUtils.trim;
import java.util.Arrays;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.WidgetSelectionState;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;

public class DisplayUtilsTest {

	private String textWithoutMarkdown = "This is the test markdown\nthat will be used.";
	private String textWithMarkdown = "This is the **test** markdown\nthat will be used.";
	private String markdownDelimiter = "*";
	private String markdownDelimiter2 = "**";

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

		// verify that when selecting outside of the widget text, it will report that widget is not selected
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

		// if the widget is at the beginning we should be able to find it when selecting inside, or the
		// first character
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


		// if the widget is at the end we should be able to find it when selecting inside, or the last
		// character
		testMarkdown = markdownText + fullSynapseWidgetText;
		DisplayUtils.updateWidgetSelectionState(state, testMarkdown, testMarkdown.length() - 1);
		assertTrue(state.isWidgetSelected());
		assertEquals(synapseWidgetInnerText, state.getInnerWidgetText());
		assertEquals(testMarkdown.length() - fullSynapseWidgetText.length(), state.getWidgetStartIndex());
		assertEquals(testMarkdown.length(), state.getWidgetEndIndex());

		// select inside, but this time let's add spaces inside one of the parameters (and continue to
		// verify that it finds the entire widget text)
		synapseWidgetInnerText += "&param3=7 8 9";
		fullSynapseWidgetText = WidgetConstants.WIDGET_START_MARKDOWN + synapseWidgetInnerText + WidgetConstants.WIDGET_END_MARKDOWN;
		testMarkdown = markdownText + fullSynapseWidgetText;
		DisplayUtils.updateWidgetSelectionState(state, testMarkdown, testMarkdown.length() - fullSynapseWidgetText.length() + 4);
		assertTrue(state.isWidgetSelected());
		assertEquals(synapseWidgetInnerText, state.getInnerWidgetText());
		assertEquals(testMarkdown.length() - fullSynapseWidgetText.length(), state.getWidgetStartIndex());
		assertEquals(testMarkdown.length(), state.getWidgetEndIndex());

		// if the widget is in the middle we should be able to find it when selecting inside
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
		// verify that when selecting outside of the widget text, it will report that widget is not selected
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
		path.setPath(Arrays.asList(new EntityHeader[] {root, project, file}));
		assertEquals(project, DisplayUtils.getProjectHeader(path));
	}

	@Test
	public void testGetDisplayName() {
		// DisplayUtils.getDisplayName(firstName, lastName, userName)
		String fName = "Strong";
		String lName = "Bad";
		String userName = "SBEmail";
		String tempUserName = WebConstants.TEMPORARY_USERNAME_PREFIX + "1234";

		// first, verify that the temp username is recognized as a temp username
		assertTrue(DisplayUtils.isTemporaryUsername(tempUserName));
		assertFalse(DisplayUtils.isTemporaryUsername(userName));

		// try combinations
		// display of an old user (first name and last name are set, but has a temp username)
		assertEquals("Strong Bad", DisplayUtils.getDisplayName(fName, lName, tempUserName));

		// possible new user state, where first and last names are not filled in during registration
		assertEquals("SBEmail", DisplayUtils.getDisplayName(null, null, userName));
		assertEquals("SBEmail", DisplayUtils.getDisplayName("", "", userName));

		// old user who has set the username
		assertEquals("Strong Bad (SBEmail)", DisplayUtils.getDisplayName(fName, lName, userName));
	}

	@Test
	public void testSurroundText() {
		// basic case, "test" selected, text should be surrounded with markdown
		int startPos = textWithoutMarkdown.indexOf("test");
		int selectionLength = "test".length();
		String result = DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
		assertEquals("This is the *test* markdown\nthat will be used.", result);
	}

	@Test
	public void testSurroundTextNoSelection() {
		// if no text is selected, then it should place the markdown around the current cursor position
		int startPos = textWithoutMarkdown.indexOf("test");
		int selectionLength = 0;
		String result = DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, true, startPos, selectionLength);
		assertEquals("This is the **test markdown\nthat will be used.", result);
	}

	@Test(expected = IllegalArgumentException.class)
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

	@Test(expected = IllegalArgumentException.class)
	public void testSurroundTextPastEndOfLine() {
		int startPos = textWithoutMarkdown.indexOf("test");
		int selectionLength = "test markdown\nthat".length();
		DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSurroundTextPastEndOfText() {
		// selection goes past end of string.
		int startPos = textWithoutMarkdown.indexOf("used");
		int selectionLength = 100;
		DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSurroundTextInvalidStart() {
		int startPos = -1;
		int selectionLength = 2;
		DisplayUtils.surroundText(textWithoutMarkdown, markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
	}

	@Test(expected = IllegalArgumentException.class)
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
		// before the selection has the markdown, but after the selection does not
		int startPos = textWithMarkdown.indexOf("test");
		int selectionLength = "test".length() + 4;
		String result = DisplayUtils.surroundText(textWithMarkdown, markdownDelimiter2, markdownDelimiter2, false, startPos, selectionLength);
		assertEquals("This is the ****test** m**arkdown\nthat will be used.", result);
	}

	@Test
	public void testSurroundTextStripMarkdownTest3() {
		// after the selection has the markdown, but before the selection does not
		int startPos = textWithMarkdown.indexOf("test") - 6;
		int selectionLength = "test".length() + 6;
		String result = DisplayUtils.surroundText(textWithMarkdown, markdownDelimiter2, markdownDelimiter2, false, startPos, selectionLength);
		assertEquals("This is **the **test**** markdown\nthat will be used.", result);
	}

	@Test
	public void testSurroundTextStripMarkdownTest4() {
		// test end of text with markdown
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
		assertEquals(markdownDelimiter + text + markdownDelimiter, result);
	}

	@Test
	public void testSurroundTextMarkdownEndOfText() {
		String textWithMarkdown = "end";
		int startPos = textWithMarkdown.length();
		int selectionLength = 0;
		String result = DisplayUtils.surroundText(textWithMarkdown, markdownDelimiter2, markdownDelimiter2, true, startPos, selectionLength);
		assertEquals(textWithMarkdown + markdownDelimiter2 + markdownDelimiter2, result);
	}

	@Test
	public void testSurroundTextNoText() {
		int startPos = 0;
		int selectionLength = 0;
		String result = DisplayUtils.surroundText("", markdownDelimiter, markdownDelimiter, false, startPos, selectionLength);
		assertEquals(markdownDelimiter + markdownDelimiter, result);
	}

	@Test
	public void testCreateEntityVersionString() {
		assertEquals("", DisplayUtils.createEntityVersionString(null, null));
		assertEquals("", DisplayUtils.createEntityVersionString("", null));
		String versioned = DisplayUtils.createEntityVersionString("syn123", null);
		assertTrue(versioned.contains("syn123"));
		versioned = DisplayUtils.createEntityVersionString("syn1234", 8888L);
		assertTrue(versioned.contains("syn1234"));
		assertTrue(versioned.contains("8888"));
	}

	@Test
	public void testParseEntityVersionString() {
		String validSynId = "syn123";
		Long validVersion = 3L;

		// verify ref without version
		Reference expectedRef = new Reference();
		expectedRef.setTargetId(validSynId);
		Reference testRef;
		testRef = DisplayUtils.parseEntityVersionString(validSynId);
		assertEquals(expectedRef, testRef);

		// verify ref with version defined using dot notation
		expectedRef.setTargetVersionNumber(validVersion);
		testRef = DisplayUtils.parseEntityVersionString(validSynId + "." + validVersion);
		assertEquals(expectedRef, testRef);
		// verify ref with version defined using "/version/" notation
		testRef = DisplayUtils.parseEntityVersionString(validSynId + WebConstants.ENTITY_VERSION_STRING + validVersion);
		assertEquals(expectedRef, testRef);
	}

	@Test
	public void testCapitalize() {
		assertEquals(null, DisplayUtils.capitalize(null));
		assertEquals("", DisplayUtils.capitalize(""));
		assertEquals("F", DisplayUtils.capitalize("f"));
		assertEquals("Hello", DisplayUtils.capitalize("heLLO"));
	}

	@Test
	public void testTrim() {
		assertEquals("", trim(null));
		assertEquals("", trim(" \t"));
		assertEquals("test", trim("  test     \t"));
	}
}

