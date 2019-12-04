package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorAction;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidgetView;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.WidgetSelectionState;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamSelector;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MarkdownEditorWidgetTest {
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	MarkdownEditorWidgetView mockView;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	WidgetRegistrar mockWidgetRegistrar;
	MarkdownEditorWidget presenter;
	@Mock
	IconsImageBundle mockIcons;
	@Mock
	CookieProvider mockCookies;
	BaseEditWidgetDescriptorPresenter mockBaseEditWidgetPresenter;
	@Mock
	ResourceLoader mockResourceLoader;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	BaseEditWidgetDescriptorPresenter mockEditDescriptor;
	@Mock
	MarkdownWidget mockMarkdownWidget;
	WikiPageKey wikiPageKey;
	String initialMarkdown;
	WikiPage testPage;
	String fileHandleId1 = "44";
	String fileHandleId2 = "45";
	@Mock
	UserTeamSelector mockUserSelector;
	@Mock
	KeyPressEvent mockKeyEvent;
	@Mock
	PortalGinInjector mockGinInjector;

	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.getMarkdownWidget()).thenReturn(mockMarkdownWidget);
		presenter = new MarkdownEditorWidget(mockView, mockSynapseClient, mockCookies, mockGwt, mockEditDescriptor, mockWidgetRegistrar, mockUserSelector, mockGinInjector);
		wikiPageKey = new WikiPageKey("syn1111", ObjectType.ENTITY.toString(), null);
		initialMarkdown = "Hello Markdown";
		presenter.configure(initialMarkdown);

		String testPageMarkdownText = "my test markdown";
		testPage = new WikiPage();
		testPage.setId("wikiPageId");
		testPage.setMarkdown(testPageMarkdownText);
		when(mockView.getMarkdown()).thenReturn(testPageMarkdownText);
		testPage.setTitle("My Test Wiki Title");
		List<String> fileHandleIds = new ArrayList<String>();
		// our page has two file handles already
		fileHandleIds.add(fileHandleId1);
		fileHandleIds.add(fileHandleId2);
		testPage.setAttachmentFileHandleIds(fileHandleIds);
		presenter.setWikiKey(wikiPageKey);
	}

	@Test
	public void testConfigure() {
		// configured in before, verify that view is reset
		verify(mockView).clear();
		verify(mockView).setAttachmentCommandsVisible(true);
		verify(mockView).setAlphaCommandsVisible(false);
		verify(mockView).showEditMode();
	}

	@Test
	public void testSetPresenter() throws Exception {
		verify(mockView).setPresenter(eq(presenter));
	}

	@Test
	public void testUserSelectorConfigure() {
		String markdown = "";
		when(mockView.getMarkdown()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(0);

		ArgumentCaptor<CallbackP> callbackCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockUserSelector).configure(callbackCaptor.capture());
		String username = "jay";
		callbackCaptor.getValue().invoke(username);

		verify(mockView).setMarkdown(username + " ");
		verify(mockView).setFocus(true);
	}

	@Test
	public void testUserSelectorModalShownHandler() {
		ArgumentCaptor<ModalShownHandler> callbackCaptor = ArgumentCaptor.forClass(ModalShownHandler.class);
		verify(mockUserSelector).addModalShownHandler(callbackCaptor.capture());
		callbackCaptor.getValue().onShown(null);

		verify(mockView).setEditorEnabled(true);
	}

	@Test
	public void testGetFormattingGuide() throws Exception {
		reset(mockSynapseClient);
		Map<String, WikiPageKey> testHelpPagesMap = new HashMap<String, WikiPageKey>();
		WikiPageKey formattingGuideWikiKey = new WikiPageKey("syn1234", ObjectType.ENTITY.toString(), null);
		testHelpPagesMap.put(WebConstants.FORMATTING_GUIDE, formattingGuideWikiKey);
		AsyncMockStubber.callSuccessWith(testHelpPagesMap).when(mockSynapseClient).getPageNameToWikiKeyMap(any(AsyncCallback.class));
		CallbackP<WikiPageKey> mockCallback = mock(CallbackP.class);
		presenter.getFormattingGuideWikiKey(mockCallback);
		// service was called
		verify(mockSynapseClient).getPageNameToWikiKeyMap(any(AsyncCallback.class));
		// and callback was invoked with the formatting guide wiki key
		ArgumentCaptor<WikiPageKey> wikiKeyCaptor = ArgumentCaptor.forClass(WikiPageKey.class);
		verify(mockCallback).invoke(wikiKeyCaptor.capture());
		assertEquals(formattingGuideWikiKey, wikiKeyCaptor.getValue());
	}

	@Test
	public void testGetFormattingGuideFailure() throws Exception {
		reset(mockSynapseClient);
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getPageNameToWikiKeyMap(any(AsyncCallback.class));
		CallbackP<WikiPageKey> mockCallback = mock(CallbackP.class);
		presenter.getFormattingGuideWikiKey(mockCallback);
		// service was called
		verify(mockSynapseClient).getPageNameToWikiKeyMap(any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testInsertMarkdownBeginning() {
		String markdown = "say hello";
		String newText = "I ";
		when(mockView.getMarkdown()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(0);

		presenter.insertMarkdown(newText);

		verify(mockView).setMarkdown(eq(newText + markdown));
		verify(mockView).setFocus(true);
	}

	@Test
	public void testInsertMarkdownEnd() {
		String markdown = "say hello";
		String newText = " to me";
		when(mockView.getMarkdown()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(markdown.length());

		presenter.insertMarkdown(newText);
		verify(mockView).setMarkdown(eq(markdown + newText));

		// should also happen when the cursor position is out of range
		reset(mockView);
		when(mockView.getMarkdown()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(-1);
		presenter.insertMarkdown(newText);
		verify(mockView).setMarkdown(eq(markdown + newText));
	}

	@Test
	public void testResizeMarkdown() {
		String markdown = "";

		when(mockView.getClientHeight()).thenReturn(0);
		presenter.resizeMarkdownTextArea();
		verify(mockView).setMarkdownTextAreaHeight(MarkdownEditorWidget.MIN_TEXTAREA_HEIGHT);

		reset(mockView);
		when(mockView.getClientHeight()).thenReturn(MarkdownEditorWidget.MIN_TEXTAREA_HEIGHT);
		presenter.resizeMarkdownTextArea();
		verify(mockView).setMarkdownTextAreaHeight(MarkdownEditorWidget.MIN_TEXTAREA_HEIGHT);

		reset(mockView);
		when(mockView.getClientHeight()).thenReturn(1000);
		presenter.resizeMarkdownTextArea();
		verify(mockView).setMarkdownTextAreaHeight(1000 - MarkdownEditorWidget.OTHER_EDITOR_COMPONENTS_HEIGHT);
	}

	@Test
	public void testInsertMarkdownMiddle() {
		String markdown = "1 2  5";
		String newText = "3 4";
		when(mockView.getMarkdown()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(4);

		presenter.insertMarkdown(newText);
		verify(mockView).setMarkdown(eq("1 2 3 4 5"));
	}

	@Test
	public void testDeleteMarkdown() {
		String markdown = "foobarfoofoofoobar";
		String deleteText = "foo";
		when(mockView.getMarkdown()).thenReturn(markdown);

		presenter.deleteMarkdown(deleteText);
		verify(mockView).setMarkdown(eq("barbar"));
	}

	@Test
	public void testDeleteMarkdownNotFound() {
		String markdown = "foobarfoofoofoobar";
		String deleteText = "bingo";
		when(mockView.getMarkdown()).thenReturn(markdown);

		presenter.deleteMarkdown(deleteText);
		verify(mockView).setMarkdown(eq(markdown));
	}

	@Test
	public void testSurroundWithTag() {
		String markdown = "make it bold";
		when(mockView.getMarkdown()).thenReturn(markdown);
		when(mockView.getSelectionLength()).thenReturn(4);
		int startCursorPos = 8;
		when(mockView.getCursorPos()).thenReturn(startCursorPos);
		String expected = "make it **bold**";
		String tag = "**";
		presenter.surroundWithTag(tag);
		verify(mockView).setMarkdown(expected);
		// set the cursor position past the start tag
		verify(mockView).setCursorPos(startCursorPos + tag.length());
		verify(mockView).setMarkdownFocus();
	}

	@Test
	public void testInsertNewWidget() {
		String contentType = WidgetConstants.VIDEO_CONTENT_TYPE;
		presenter.insertNewWidget(contentType);
		verify(mockEditDescriptor).editNew(wikiPageKey, contentType);

		// verify that a widget descriptor update handler is added, and when fired it sends back to our
		// handler that we passed as input to the configure.
		ArgumentCaptor<WidgetDescriptorUpdatedHandler> captor = ArgumentCaptor.forClass(WidgetDescriptorUpdatedHandler.class);
		verify(mockEditDescriptor).addWidgetDescriptorUpdatedHandler(captor.capture());
		WidgetDescriptorUpdatedEvent event = new WidgetDescriptorUpdatedEvent();
		captor.getValue().onUpdate(event);
	}

	@Test
	public void testEditWidgetNoneSelected() {
		presenter.getWidgetSelectionState().setWidgetSelected(false);
		presenter.editExistingWidget();
		verify(mockEditDescriptor, Mockito.never()).editExisting(any(WikiPageKey.class), anyString(), any(Map.class));
	}

	@Test
	public void testEditWidget() {
		String before = "Pretend this contains a ";
		String widgetDefinition = "{widget definition}";
		String after = " inside of it";
		String md = before + widgetDefinition + after;
		when(mockView.getMarkdown()).thenReturn(md);
		int startWidgetIndex = md.indexOf(widgetDefinition);
		int endWidgetIndex = startWidgetIndex + widgetDefinition.length();

		WidgetSelectionState selectionState = presenter.getWidgetSelectionState();
		selectionState.setInnerWidgetText(widgetDefinition);
		selectionState.setWidgetStartIndex(startWidgetIndex);
		selectionState.setWidgetEndIndex(endWidgetIndex);
		selectionState.setWidgetSelected(true);

		String contentType = WidgetConstants.YOUTUBE_CONTENT_TYPE;
		Map<String, String> mockWidgetDescriptor = mock(Map.class);
		when(mockWidgetRegistrar.getWidgetContentType(widgetDefinition)).thenReturn(contentType);
		when(mockWidgetRegistrar.getWidgetDescriptor(widgetDefinition)).thenReturn(mockWidgetDescriptor);

		// call editExistingWidget via handleCommand
		presenter.handleCommand(MarkdownEditorAction.EDIT_WIDGET);

		// it should have selected the existing widget definition
		verify(mockView).setSelectionRange(startWidgetIndex, widgetDefinition.length());

		verify(mockEditDescriptor).editExisting(wikiPageKey, contentType, mockWidgetDescriptor);

		// verify that a widget descriptor update handler is added, and when fired it removes the old widget
		// markdown (to replace it with the updated value)
		ArgumentCaptor<WidgetDescriptorUpdatedHandler> captor = ArgumentCaptor.forClass(WidgetDescriptorUpdatedHandler.class);
		verify(mockEditDescriptor).addWidgetDescriptorUpdatedHandler(captor.capture());
		WidgetDescriptorUpdatedEvent event = new WidgetDescriptorUpdatedEvent();
		captor.getValue().onUpdate(event);
		// it also passes the update up
		verify(mockView).setMarkdown(before + after);
		verify(mockView).setCursorPos(startWidgetIndex);
	}

	@Test
	public void testHandleCommandInsertAttachment() {
		String contentType = WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_ATTACHMENT);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertButtonLink() {
		String contentType = WidgetConstants.BUTTON_LINK_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_BUTTON_LINK);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertEntityList() {
		String contentType = WidgetConstants.ENTITYLIST_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_ENTITY_LIST);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertImage() {
		String contentType = WidgetConstants.IMAGE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_IMAGE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertExternalImage() {
		String contentType = WidgetConstants.IMAGE_LINK_EDITOR_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_IMAGE_LINK);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertLink() {
		String contentType = WidgetConstants.LINK_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_LINK);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertProvGraph() {
		String contentType = WidgetConstants.PROVENANCE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_PROV_GRAPH);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertQueryTable() {
		String contentType = WidgetConstants.QUERY_TABLE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_QUERY_TABLE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertSynapseTable() {
		String contentType = WidgetConstants.SYNAPSE_TABLE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_SYNAPSE_TABLE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertReference() {
		String contentType = WidgetConstants.REFERENCE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_REFERENCE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertTable() {
		String contentType = WidgetConstants.TABBED_TABLE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_TABLE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertUserTeam() {
		String contentType = WidgetConstants.USER_TEAM_BADGE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_USER_TEAM_BADGE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertVideo() {
		String contentType = WidgetConstants.VIDEO_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_VIDEO);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertExternalWebsite() {
		String contentType = WidgetConstants.SHINYSITE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_EXTERNAL_WEBSITE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandInsertAPISuperTable() {
		String contentType = WidgetConstants.API_TABLE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_API_SUPERTABLE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandJoinTeam() {
		String contentType = WidgetConstants.JOIN_TEAM_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_JOIN_TEAM);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	private String surroundTextMarkdown = "this";

	private String getNewMarkdown() {
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockView).setMarkdown(captor.capture());
		return captor.getValue();
	}

	private void setupSurroundText() {
		when(mockView.getSelectionLength()).thenReturn(surroundTextMarkdown.length());
		when(mockView.getMarkdown()).thenReturn(surroundTextMarkdown);
		when(mockView.getCursorPos()).thenReturn(0);
	}

	@Test
	public void testHandleCommandBold() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.BOLD);
		assertEquals("**this**", getNewMarkdown());
	}

	@Test
	public void testHandleCommandItalic() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.ITALIC);
		assertEquals("_this_", getNewMarkdown());
	}

	@Test
	public void testHandleCommandStrikeThrough() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.STRIKETHROUGH);
		assertEquals("--this--", getNewMarkdown());
	}

	@Test
	public void testHandleCommandCodeBlock() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.CODE_BLOCK);
		assertEquals("\n```\nthis\n```\n", getNewMarkdown());
	}

	@Test
	public void testHandleCommandMath() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.MATH);
		assertEquals("$$\\(this\\)$$", getNewMarkdown());
	}

	@Test
	public void testHandleCommandSubscript() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.SUBSCRIPT);
		assertEquals("~this~", getNewMarkdown());
	}

	@Test
	public void testHandleCommandSuperscript() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.SUPERSCRIPT);
		assertEquals("^this^", getNewMarkdown());
	}

	@Test
	public void testHandleCommandH1() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H1);
		assertEquals("#this", getNewMarkdown());
	}

	@Test
	public void testHandleCommandH2() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H2);
		assertEquals("##this", getNewMarkdown());
	}

	@Test
	public void testHandleCommandH3() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H3);
		assertEquals("###this", getNewMarkdown());
	}

	@Test
	public void testHandleCommandH4() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H4);
		assertEquals("####this", getNewMarkdown());
	}

	@Test
	public void testHandleCommandH5() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H5);
		assertEquals("#####this", getNewMarkdown());
	}

	@Test
	public void testHandleCommandH6() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H6);
		assertEquals("######this", getNewMarkdown());
	}

	// insert markdown commands

	@Test
	public void testHandleCommandSubmitToEvaluation() {
		String contentType = WidgetConstants.SUBMIT_TO_EVALUATION_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_SUBMIT_TO_EVALUATION);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}

	@Test
	public void testHandleCommandToC() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.INSERT_TOC);
		assertTrue(getNewMarkdown().contains(WidgetConstants.TOC_CONTENT_TYPE));
	}

	@Test
	public void testHandleCommandWikiFilesPreview() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.INSERT_WIKI_FILES_PREVIEW);
		assertTrue(getNewMarkdown().contains(WidgetConstants.WIKI_FILES_PREVIEW_CONTENT_TYPE));
	}

	@Test
	public void testHandleCommandTutorialWizard() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.INSERT_TUTORIAL_WIZARD);
		assertTrue(getNewMarkdown().contains(WidgetConstants.TUTORIAL_WIZARD_CONTENT_TYPE));
	}

	@Test
	public void testHideAttachmentCommands() {
		presenter.hideUploadRelatedCommands();
		verify(mockView).setAttachmentCommandsVisible(false);
		verify(mockView).setImageCommandsVisible(false);
		verify(mockView).setVideoCommandsVisible(false);
	}

	@Test
	public void testShowExternalImageButton() {
		presenter.showExternalImageButton();
		verify(mockView).setExternalImageCommandVisible(true);
	}

	@Test
	public void testHandleInsertUserLinkCommand() {
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.INSERT_USER_LINK);
		assertTrue(getNewMarkdown().contains("@"));
		verify(mockUserSelector).show();
	}

	@Test
	public void testOnKeyPress() {
		when(mockKeyEvent.getCharCode()).thenReturn('1');
		presenter.onKeyPress(mockKeyEvent);
		verify(mockUserSelector, never()).show();
		when(mockKeyEvent.getCharCode()).thenReturn('a');
		presenter.onKeyPress(mockKeyEvent);
		verify(mockUserSelector, never()).show();
		verify(mockView, never()).setEditorEnabled(anyBoolean());
		when(mockKeyEvent.getCharCode()).thenReturn('@');
		presenter.onKeyPress(mockKeyEvent);
		verify(mockUserSelector).show();
		verify(mockKeyEvent).preventDefault();
		verify(mockKeyEvent).stopPropagation();
	}

	@Test
	public void testOnKeyPressBeginningOfMarkdown() {
		when(mockView.getCursorPos()).thenReturn(0);
		when(mockView.getMarkdown()).thenReturn("");
		when(mockGwt.isWhitespace(anyString())).thenReturn(false);
		when(mockKeyEvent.getCharCode()).thenReturn('@');
		presenter.onKeyPress(mockKeyEvent);
		verify(mockUserSelector).show();
	}

	@Test
	public void testOnKeyPressNotWhitespace() {
		// typing an @ at the end of the string 'email'
		when(mockView.getCursorPos()).thenReturn(5);
		when(mockView.getMarkdown()).thenReturn("email");
		when(mockGwt.isWhitespace(anyString())).thenReturn(false);
		when(mockKeyEvent.getCharCode()).thenReturn('@');
		presenter.onKeyPress(mockKeyEvent);
		verify(mockUserSelector, never()).show();
	}

	@Test
	public void testPreview() throws Exception {
		presenter.previewClicked();
		verify(mockMarkdownWidget).configure(anyString(), any(WikiPageKey.class), any(Long.class));
		verify(mockView).showPreview();
	}

	@Test
	public void testHandleCommandTeamMemberCount() {
		presenter.handleCommand(MarkdownEditorAction.INSERT_TEAM_MEMBER_COUNT);

		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(WidgetConstants.TEAM_MEMBER_COUNT_CONTENT_TYPE));
	}

	@Test
	public void testHandleCommandTeamMembers() {
		presenter.handleCommand(MarkdownEditorAction.INSERT_TEAM_MEMBERS);

		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(WidgetConstants.TEAM_MEMBERS_CONTENT_TYPE));
	}
}
