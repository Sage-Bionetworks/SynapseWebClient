package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditorView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class WikiMarkdownEditorTest {
	SynapseClientAsync mockSynapseClient; 
	WikiMarkdownEditorView mockView;
	SynapseJSNIUtils mockSynapseJSNIUtils; 
	PlaceChanger mockPlaceChanger;
	WikiMarkdownEditor presenter;
	MarkdownWidget mockMarkdownWidget;
	WikiPageKey wikiPageKey;
	String initialMarkdown;
	CallbackP<WikiPage> mockDescriptorUpdatedHandler;
	GlobalApplicationState mockGlobalApplicationState;
	WikiPage testPage;
	String fileHandleId1 = "44";
	String fileHandleId2 = "45";
	
	@Mock
	MarkdownEditorWidget mockMarkdownEditorWidget;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(WikiMarkdownEditorView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockMarkdownWidget = mock(MarkdownWidget.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		presenter = new WikiMarkdownEditor(mockView, mockMarkdownEditorWidget, mockSynapseClient, mockGlobalApplicationState, mockMarkdownWidget);
		wikiPageKey = new WikiPageKey("syn1111", ObjectType.ENTITY.toString(), null);
		mockDescriptorUpdatedHandler = mock(CallbackP.class);
		initialMarkdown = "Hello Markdown";
		
		String testPageMarkdownText = "my test markdown";
		testPage = new WikiPage();
		testPage.setId("wikiPageId");
		testPage.setMarkdown(testPageMarkdownText);
		when(mockMarkdownEditorWidget.getMarkdown()).thenReturn(testPageMarkdownText);
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
		presenter.configure(wikiPageKey, mockDescriptorUpdatedHandler);
	}
	
	
	@Test
	public void testConfigure() {
		//configured in before, verify that view is reset
		verify(mockView).setPresenter(presenter);
		verify(mockView).setMarkdownPreviewWidget(any(Widget.class));
		verify(mockView).setMarkdownEditorWidget(any(Widget.class));
		verify(mockView).clear();
		verify(mockMarkdownEditorWidget).configure(anyString());
		verify(mockView).setTitleEditorVisible(false);
		verify(mockGlobalApplicationState).setIsEditing(true);
		verify(mockView).showEditorModal();
	}
	
	@Test
	public void testPreview() throws Exception {
		presenter.previewClicked();
		verify(mockMarkdownWidget).configure(anyString(), any(WikiPageKey.class), any(Long.class));
		verify(mockView).showPreviewModal();
	}
	
	@Test
	public void testPreviewFailure() throws Exception {
		presenter.previewClicked();
		verify(mockMarkdownWidget).configure(anyString(), any(WikiPageKey.class), any(Long.class));
		verify(mockView).showPreviewModal();
	}
	
	@Test
	public void testDeleteConfirmedCallback() {
		boolean isConfirmed = true;

		presenter.deleteClicked();
		ArgumentCaptor<ConfirmCallback> captor = ArgumentCaptor.forClass(ConfirmCallback.class);
		verify(mockView).confirm(anyString(), captor.capture());
		//confirm deletion
		captor.getValue().callback(isConfirmed);
		verify(mockSynapseClient).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockView).hideEditorModal();
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}
	
	@Test
	public void testDeleteCancelCallback() {
		boolean isConfirmed = false;
		
		presenter.deleteClicked();
		ArgumentCaptor<ConfirmCallback> captor = ArgumentCaptor.forClass(ConfirmCallback.class);
		verify(mockView).confirm(anyString(), captor.capture());
		//confirm deletion cancelled
		captor.getValue().callback(isConfirmed);
		verify(mockSynapseClient, Mockito.never()).deleteV2WikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testSave() {
		presenter.configure(testPage);
		reset(mockView);
		presenter.saveClicked();
		verify(mockView).setSaving(true);
		verify(mockSynapseClient).updateV2WikiPageWithV1(eq(wikiPageKey.getOwnerObjectId()), eq(wikiPageKey.getOwnerObjectType()), eq(testPage), any(AsyncCallback.class));
	}
	
	@Test
	public void testCancel() {
		//tests setActionHandler as well
		reset(mockView);
		presenter.cancelClicked();
		verify(mockView).hideEditorModal();
	}
	
	@Test
	public void testAddAttachments() throws IOException, RestServiceException, JSONObjectAdapterException{		
		presenter.configure(testPage);
		String fileHandleId3 = "46";
		
		List<String> newFileHandles = new ArrayList<String>();
		newFileHandles.add(fileHandleId2);
		newFileHandles.add(fileHandleId3);
		presenter.filesAdded(newFileHandles);
		
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
		
		presenter.filesRemoved(deleteHandleIds);
		List<String> currentFileHandleIds = presenter.getWikiPage().getAttachmentFileHandleIds();
		assertTrue(currentFileHandleIds.size() == 1);
		assertTrue(currentFileHandleIds.contains(fileHandleId1));
	}
	
	
}
