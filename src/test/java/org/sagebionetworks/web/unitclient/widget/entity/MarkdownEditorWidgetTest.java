package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorAction;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidgetView;
import org.sagebionetworks.web.client.widget.entity.WidgetSelectionState;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MarkdownEditorWidgetTest {
	SynapseClientAsync mockSynapseClient; 
	MarkdownEditorWidgetView mockView;
	SynapseJSNIUtils mockSynapseJSNIUtils; 
	WidgetRegistrar mockWidgetRegistrar;
	PlaceChanger mockPlaceChanger;
	MarkdownEditorWidget presenter;
	IconsImageBundle mockIcons;
	CookieProvider mockCookies;
	BaseEditWidgetDescriptorPresenter mockBaseEditWidgetPresenter;
	ResourceLoader mockResourceLoader;
	GWTWrapper mockGwt;
	BaseEditWidgetDescriptorPresenter mockEditDescriptor;
	WikiPageKey wikiPageKey;
	String initialMarkdown;
	CallbackP<WikiPage> mockDescriptorUpdatedHandler;
	GlobalApplicationState mockGlobalApplicationState;
	WikiPage testPage;
	String fileHandleId1 = "44";
	String fileHandleId2 = "45";

	@Before
	public void before() throws JSONObjectAdapterException {
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockIcons = mock(IconsImageBundle.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		mockBaseEditWidgetPresenter = mock(BaseEditWidgetDescriptorPresenter.class);
		mockResourceLoader = mock(ResourceLoader.class);
		mockCookies = mock(CookieProvider.class);
		mockGwt = mock(GWTWrapper.class);
		mockView = mock(MarkdownEditorWidgetView.class);
		mockEditDescriptor = mock(BaseEditWidgetDescriptorPresenter.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		presenter = new MarkdownEditorWidget(mockView, mockSynapseClient, mockCookies, mockGwt, mockEditDescriptor, mockWidgetRegistrar, mockGlobalApplicationState);
		
		wikiPageKey = new WikiPageKey("syn1111", ObjectType.ENTITY.toString(), null);
		mockDescriptorUpdatedHandler = mock(CallbackP.class);
		initialMarkdown = "Hello Markdown";
		presenter.configure(wikiPageKey, mockDescriptorUpdatedHandler);
		
		String testPageMarkdownText = "my test markdown";
		testPage = new WikiPage();
		testPage.setId("wikiPageId");
		testPage.setMarkdown(testPageMarkdownText);
		when(mockView.getMarkdownText()).thenReturn(testPageMarkdownText);
		testPage.setTitle("My Test Wiki Title");
		List<String> fileHandleIds = new ArrayList<String>();
		//our page has two file handles already
		fileHandleIds.add(fileHandleId1);
		fileHandleIds.add(fileHandleId2);
		testPage.setAttachmentFileHandleIds(fileHandleIds);
		
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		WikiPage fakeWiki = new WikiPage();
		fakeWiki.setMarkdown("Fake wiki");
		AsyncMockStubber.callSuccessWith(fakeWiki).when(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class), any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith(fakeWiki).when(mockSynapseClient).updateV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
	}
	
	
	@Test
	public void testConfigure() {
		//configured in before, verify that view is reset
		verify(mockView).clear();
		verify(mockView).setAttachmentCommandsVisible(true);
		verify(mockView).setAlphaCommandsVisible(false);
		
	}
	
	@Test
	public void testSetPresenter() throws Exception {
		verify(mockView).setPresenter(eq(presenter));
	}
	
	@Test
	public void testGetFormattingGuide() throws Exception {
		reset(mockSynapseClient);
		Map<String,WikiPageKey> testHelpPagesMap = new HashMap<String, WikiPageKey>();
		WikiPageKey formattingGuideWikiKey = new WikiPageKey("syn1234", ObjectType.ENTITY.toString(), null);
		testHelpPagesMap.put(WebConstants.FORMATTING_GUIDE, formattingGuideWikiKey);
		AsyncMockStubber
				.callSuccessWith(testHelpPagesMap)
				.when(mockSynapseClient)
				.getHelpPages(any(AsyncCallback.class));
		CallbackP<WikiPageKey> mockCallback = mock(CallbackP.class);
		presenter.getFormattingGuideWikiKey(mockCallback);
		//service was called
		verify(mockSynapseClient).getHelpPages(any(AsyncCallback.class));
		//and callback was invoked with the formatting guide wiki key
		ArgumentCaptor<WikiPageKey> wikiKeyCaptor = ArgumentCaptor.forClass(WikiPageKey.class);
		verify(mockCallback).invoke(wikiKeyCaptor.capture());
		assertEquals(formattingGuideWikiKey, wikiKeyCaptor.getValue());
	}
	
	@Test
	public void testGetFormattingGuideFailure() throws Exception {
		reset(mockSynapseClient);
		AsyncMockStubber
				.callFailureWith(new Exception())
				.when(mockSynapseClient)
				.getHelpPages(any(AsyncCallback.class));
		CallbackP<WikiPageKey> mockCallback = mock(CallbackP.class);
		presenter.getFormattingGuideWikiKey(mockCallback);
		//service was called
		verify(mockSynapseClient).getHelpPages(any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testPreview() throws Exception {
		String htmlReturned = "<h1>Html returned</h2>";
		final String testMarkdown = "HTML Returns\n----------";
		AsyncMockStubber
				.callSuccessWith(htmlReturned)
				.when(mockSynapseClient)
				.markdown2Html(anyString(), anyBoolean(), anyBoolean(),anyString(),
						any(AsyncCallback.class));
		
		//call showPreview through handleCommand
		presenter.handleCommand(MarkdownEditorAction.PREVIEW);
		verify(mockSynapseClient).markdown2Html(anyString(), anyBoolean(), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockView).showPreviewHTML(htmlReturned, wikiPageKey, mockWidgetRegistrar);
	}
	
	@Test
	public void testPreviewFailure() throws Exception {
		AsyncMockStubber
				.callFailureWith(new Exception())
				.when(mockSynapseClient)
				.markdown2Html(anyString(), anyBoolean(), anyBoolean(),anyString(),
						any(AsyncCallback.class));
		
		//call showPreview through handleCommand
		presenter.handleCommand(MarkdownEditorAction.PREVIEW);
		verify(mockSynapseClient).markdown2Html(anyString(), anyBoolean(), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testSave() {
		presenter.configure(testPage);
		reset(mockView);
		presenter.handleCommand(MarkdownEditorAction.SAVE);
		verify(mockView).setSaving(true);
		verify(mockSynapseClient).updateV2WikiPageWithV1(eq(wikiPageKey.getOwnerObjectId()), eq(wikiPageKey.getOwnerObjectType()), eq(testPage), any(AsyncCallback.class));
	}
	
	@Test
	public void testCancel() {
		//tests setActionHandler as well
		reset(mockView);
		presenter.handleCommand(MarkdownEditorAction.CANCEL);
		verify(mockView).hideEditorModal();
	}
	
	@Test
	public void testDelete() {
		presenter.configure(testPage);
		//tests setActionHandler as well
		reset(mockView);
		presenter.handleCommand(MarkdownEditorAction.DELETE);
		verify(mockSynapseClient).deleteV2WikiPage(eq(wikiPageKey), any(AsyncCallback.class));
		verify(mockView).hideEditorModal();
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}
	
	@Test
	public void testInsertMarkdownBeginning() {
		String markdown = "say hello";
		String newText = "I ";
		when(mockView.getMarkdown()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(0);
		
		presenter.insertMarkdown(newText);
		
		verify(mockView).setMarkdown(eq(newText + markdown));
	}
	
	@Test
	public void testInsertMarkdownEnd() {
		String markdown = "say hello";
		String newText = " to me";
		when(mockView.getMarkdown()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(markdown.length());
		
		presenter.insertMarkdown(newText);
		verify(mockView).setMarkdown(eq(markdown + newText));
		
		//should also happen when the cursor position is out of range
		reset(mockView);
		when(mockView.getMarkdown()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(-1);
		presenter.insertMarkdown(newText);
		verify(mockView).setMarkdown(eq(markdown + newText));
	}
	
	@Test
	public void testResizeMarkdownEmpty() {
		String markdown = "";
		when(mockView.getMarkdownTextAreaVisibleLines()).thenReturn(0);
		when(mockView.getMarkdownText()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(4);
		presenter.resizeMarkdownTextArea();
		verify(mockView).resizeMarkdownTextArea(5);
		String newText = "1\n2\n3\n4";
		when(mockView.getMarkdownTextAreaVisibleLines()).thenReturn(5);
		when(mockView.getMarkdownText()).thenReturn(newText);
		presenter.resizeMarkdownTextArea();
		//shouldn't resize at this size, therefore remaining at one invocation
		verify(mockView).resizeMarkdownTextArea(5);
		//should resize after 5 line threshold
		newText += "\n5\n6\n7";
		when(mockView.getMarkdownTextAreaVisibleLines()).thenReturn(5);
		when(mockView.getMarkdownText()).thenReturn(newText);
		presenter.resizeMarkdownTextArea();
		//7 lines, so should show an 8th line below
		verify(mockView).resizeMarkdownTextArea(8);
	}
	
	@Test
	public void testResizeMarkdownLongText() {
		String markdown = "1\n2\n3\n4\n5\n6\n7\n8\n9\n";
		when(mockView.getMarkdownTextAreaVisibleLines()).thenReturn(0);
		when(mockView.getMarkdownText()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(4);
		presenter.resizeMarkdownTextArea();
		verify(mockView).resizeMarkdownTextArea(10);
	}	
	
	@Test
	public void testResizeMarkdownOnInsert() {
		String markdown = "1\n2\n3\n4\n5\n6\n7\n8";
		String newText = "\n9\n10";
		when(mockView.getMarkdownTextAreaVisibleLines()).thenReturn(0);
		when(mockView.getMarkdownText()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(4);
		presenter.resizeMarkdownTextArea();
		//originally 9 lines (8 lines + 1 below)
		verify(mockView).resizeMarkdownTextArea(9);
		
		markdown += newText;
		when(mockView.getMarkdownTextAreaVisibleLines()).thenReturn(9);
		when(mockView.getMarkdownText()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(4);
		presenter.resizeMarkdownTextArea();
		//now should be 11 lines (10 lines + 1 below)
		verify(mockView).resizeMarkdownTextArea(11);
	}
	
	@Test
	public void testResizeMarkdownOnDelete() {
		String markdown = "1\n2\n3\n4\n5\n6\n7\n8";
		when(mockView.getMarkdownTextAreaVisibleLines()).thenReturn(0);
		when(mockView.getMarkdownText()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(4);
		presenter.resizeMarkdownTextArea();
		//originally 9 lines (8 lines + 1 below)
		verify(mockView).resizeMarkdownTextArea(9);
		
		markdown = "1\n2\n3\n4\n5\n6\n7";
		when(mockView.getMarkdownTextAreaVisibleLines()).thenReturn(9);
		when(mockView.getMarkdownText()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(4);
		presenter.resizeMarkdownTextArea();
		//now should be 11 lines (10 lines + 1 below)
		verify(mockView).resizeMarkdownTextArea(8);
		
		markdown = "1\n2\n3\n";
		when(mockView.getMarkdownTextAreaVisibleLines()).thenReturn(9);
		when(mockView.getMarkdownText()).thenReturn(markdown);
		when(mockView.getCursorPos()).thenReturn(4);
		presenter.resizeMarkdownTextArea();
		//now should be 11 lines (10 lines + 1 below)
		verify(mockView).resizeMarkdownTextArea(5);
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
		//set the cursor position past the start tag
		verify(mockView).setCursorPos(startCursorPos+tag.length());
		verify(mockView).setMarkdownFocus();
	}
	
	@Test
	public void testInsertNewWidget() {
		String contentType = WidgetConstants.VIDEO_CONTENT_TYPE;
		presenter.insertNewWidget(contentType);
		verify(mockEditDescriptor).editNew(wikiPageKey, contentType);
		
		//verify that a widget descriptor update handler is added, and when fired it sends back to our handler that we passed as input to the configure.
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
		
		//call editExistingWidget via handleCommand
		presenter.handleCommand(MarkdownEditorAction.EDIT_WIDGET);
		
		//it should have selected the existing widget definition
		verify(mockView).setSelectionRange(startWidgetIndex, widgetDefinition.length());
		
		verify(mockEditDescriptor).editExisting(wikiPageKey, contentType, mockWidgetDescriptor);
		
		//verify that a widget descriptor update handler is added, and when fired it removes the old widget markdown (to replace it with the updated value)
		ArgumentCaptor<WidgetDescriptorUpdatedHandler> captor = ArgumentCaptor.forClass(WidgetDescriptorUpdatedHandler.class);
		verify(mockEditDescriptor).addWidgetDescriptorUpdatedHandler(captor.capture());
		WidgetDescriptorUpdatedEvent event = new WidgetDescriptorUpdatedEvent();
		captor.getValue().onUpdate(event);
		//it also passes the update up
		verify(mockView).setMarkdown(before + after);
		verify(mockView).setCursorPos(startWidgetIndex);
	}
	
	@Test
	public void testHandleCommandInsertAttachment(){
		String contentType = WidgetConstants.ATTACHMENT_PREVIEW_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_ATTACHMENT);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	
	@Test
	public void testHandleCommandInsertButtonLink(){
		String contentType = WidgetConstants.BUTTON_LINK_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_BUTTON_LINK);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	
	@Test
	public void testHandleCommandInsertEntityList(){
		String contentType = WidgetConstants.ENTITYLIST_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_ENTITY_LIST);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	
	@Test
	public void testHandleCommandInsertImage(){
		String contentType = WidgetConstants.IMAGE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_IMAGE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	
	@Test
	public void testHandleCommandInsertLink(){
		String contentType = WidgetConstants.LINK_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_LINK);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	@Test
	public void testHandleCommandInsertProvGraph(){
		String contentType = WidgetConstants.PROVENANCE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_PROV_GRAPH);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	@Test
	public void testHandleCommandInsertQueryTable(){
		String contentType = WidgetConstants.QUERY_TABLE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_QUERY_TABLE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	@Test
	public void testHandleCommandInsertSynapseTable(){
		String contentType = WidgetConstants.SYNAPSE_TABLE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_SYNAPSE_TABLE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	
	@Test
	public void testHandleCommandInsertReference(){
		String contentType = WidgetConstants.REFERENCE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_REFERENCE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	@Test
	public void testHandleCommandInsertTable(){
		String contentType = WidgetConstants.TABBED_TABLE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_TABLE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	@Test
	public void testHandleCommandInsertUserTeam(){
		String contentType = WidgetConstants.USER_TEAM_BADGE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_USER_TEAM_BADGE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	@Test
	public void testHandleCommandInsertVideo(){
		String contentType = WidgetConstants.VIDEO_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_VIDEO);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	
	@Test
	public void testHandleCommandInsertYouTube(){
		String contentType = WidgetConstants.YOUTUBE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_YOU_TUBE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	@Test
	public void testHandleCommandInsertBookmark(){
		String contentType = WidgetConstants.BOOKMARK_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_BOOKMARK);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	@Test
	public void testHandleCommandInsertExternalWebsite(){
		String contentType = WidgetConstants.SHINYSITE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_EXTERNAL_WEBSITE);
		verify(mockEditDescriptor).editNew(eq(wikiPageKey), eq(contentType));
	}
	@Test
	public void testHandleCommandInsertAPISuperTable(){
		String contentType = WidgetConstants.API_TABLE_CONTENT_TYPE;
		presenter.handleCommand(MarkdownEditorAction.INSERT_API_SUPERTABLE);
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
	public void testHandleCommandBold(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.BOLD);
		assertEquals("**this**", getNewMarkdown());
	}
	
	@Test
	public void testHandleCommandItalic(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.ITALIC);
		assertEquals("_this_", getNewMarkdown());
	}
	
	@Test
	public void testHandleCommandStrikeThrough(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.STRIKETHROUGH);
		assertEquals("--this--", getNewMarkdown());
	}
	@Test
	public void testHandleCommandCodeBlock(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.CODE_BLOCK);
		assertEquals("\n```\nthis\n```\n", getNewMarkdown());
	}
	@Test
	public void testHandleCommandMath(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.MATH);
		assertEquals("$$\\(this\\)$$", getNewMarkdown());
	}
	@Test
	public void testHandleCommandSubscript(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.SUBSCRIPT);
		assertEquals("~this~", getNewMarkdown());
	}
	@Test
	public void testHandleCommandSuperscript(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.SUPERSCRIPT);
		assertEquals("^this^", getNewMarkdown());
	}
	@Test
	public void testHandleCommandH1(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H1);
		assertEquals("#this", getNewMarkdown());
	}
	@Test
	public void testHandleCommandH2(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H2);
		assertEquals("##this", getNewMarkdown());
	}
	@Test
	public void testHandleCommandH3(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H3);
		assertEquals("###this", getNewMarkdown());
	}
	@Test
	public void testHandleCommandH4(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H4);
		assertEquals("####this", getNewMarkdown());
	}
	@Test
	public void testHandleCommandH5(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H5);
		assertEquals("#####this", getNewMarkdown());
	}
	@Test
	public void testHandleCommandH6(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.H6);
		assertEquals("######this", getNewMarkdown());
	}
	
	//insert markdown commands
	@Test
	public void testHandleCommandJoinTeam(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.INSERT_JOIN_TEAM);
		assertTrue(getNewMarkdown().contains(WidgetConstants.JOIN_TEAM_CONTENT_TYPE));
	}
	
	@Test
	public void testHandleCommandSubmitToEvaluation(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.INSERT_SUBMIT_TO_EVALUATION);
		assertTrue(getNewMarkdown().contains(WidgetConstants.SUBMIT_TO_EVALUATION_CONTENT_TYPE));
	}
	
	@Test
	public void testHandleCommandToC(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.INSERT_TOC);
		assertTrue(getNewMarkdown().contains(WidgetConstants.TOC_CONTENT_TYPE));
	}
	@Test
	public void testHandleCommandWikiFilesPreview(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.INSERT_WIKI_FILES_PREVIEW);
		assertTrue(getNewMarkdown().contains(WidgetConstants.WIKI_FILES_PREVIEW_CONTENT_TYPE));
	}

	@Test
	public void testHandleCommandTutorialWizard(){
		setupSurroundText();
		presenter.handleCommand(MarkdownEditorAction.INSERT_TUTORIAL_WIZARD);
		assertTrue(getNewMarkdown().contains(WidgetConstants.TUTORIAL_WIZARD_CONTENT_TYPE));
	}
	

	@Test
	public void testAddAttachments() throws IOException, RestServiceException, JSONObjectAdapterException{		
		presenter.configure(testPage);
		String fileHandleId3 = "46";
		
		List<String> newFileHandles = new ArrayList<String>();
		newFileHandles.add(fileHandleId2);
		newFileHandles.add(fileHandleId3);
		presenter.addFileHandles(newFileHandles);
		
		List<String> currentFileHandleIds = presenter.getWikiPage().getAttachmentFileHandleIds();
		//should be unique values only, so there should be 3
		assertTrue(currentFileHandleIds.size() == 3);
		assertTrue(currentFileHandleIds.contains(fileHandleId1));
		assertTrue(currentFileHandleIds.contains(fileHandleId2));
		assertTrue(currentFileHandleIds.contains(fileHandleId3));
	}
	
	@Test
	public void testDeleteAttachments() throws IOException, RestServiceException, JSONObjectAdapterException{
		presenter.configure(testPage);
		List<String> deleteHandleIds = new ArrayList<String>();
		deleteHandleIds.add(fileHandleId2);
		
		presenter.removeFileHandles(deleteHandleIds);
		List<String> currentFileHandleIds = presenter.getWikiPage().getAttachmentFileHandleIds();
		assertTrue(currentFileHandleIds.size() == 1);
		assertTrue(currentFileHandleIds.contains(fileHandleId1));
	}
	
}
