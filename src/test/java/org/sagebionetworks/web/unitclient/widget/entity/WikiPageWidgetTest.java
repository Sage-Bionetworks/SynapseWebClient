package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the preview widget.
 * @author jayhodgson
 *
 */
public class WikiPageWidgetTest {
	WikiPageWidgetView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	JSONObjectAdapter mockJsonObjectAdapter;
	AdapterFactory adapterFactory = new JSONObjectAdapterImpl();
	WikiPageWidget presenter;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;

	WikiPage testPage;
	private static final String MY_TEST_ENTITY_OWNER_NAME = "My Test Entity Owner Name";
	
	@Before
	public void before() throws Exception{
		mockView = mock(WikiPageWidgetView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockJsonObjectAdapter = new JSONObjectAdapterImpl();
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		presenter = new WikiPageWidget(mockView, mockSynapseClient,
				mockNodeModelCreator, mockJsonObjectAdapter, adapterFactory,
				mockGlobalApplicationState, mockAuthenticationController);
		BatchResults<EntityHeader> headers = new BatchResults<EntityHeader>();
		headers.setTotalNumberOfResults(1);
		List<EntityHeader> resultHeaderList = new ArrayList<EntityHeader>();
		EntityHeader testEntityHeader = new EntityHeader();
		testEntityHeader.setName(MY_TEST_ENTITY_OWNER_NAME);
		testEntityHeader.setType(Project.class.getName());
		resultHeaderList.add(testEntityHeader);
		headers.setResults(resultHeaderList);
		when(mockNodeModelCreator.createBatchResults(anyString(), any(Class.class))).thenReturn(headers);
		AsyncMockStubber.callSuccessWith("fake json response").when(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));
		
		testPage = new WikiPage();
		testPage.setId("wikiPageId");
		testPage.setMarkdown("my test markdown");
		testPage.setTitle("My Test Wiki Title");

		when(mockNodeModelCreator.createJSONEntity("fake json response", WikiPage.class)).thenReturn(testPage);
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		WikiPage fakeWiki = new WikiPage();
		fakeWiki.setMarkdown("Fake wiki");
		AsyncMockStubber.callSuccessWith(fakeWiki).when(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testAsWidget(){
		presenter.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() throws JSONObjectAdapterException{
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		verify(mockView).configure(anyString(), any(WikiPageKey.class), anyString(), anyBoolean(), anyBoolean(), eq(false), eq(true), any(Long.class), eq(true));
	}
	
	@Test
	public void testConfigureNoWikiPageCannotEditIsEmbedded(){
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), false, null, true);
		verify(mockView).showWarningMessageInPage(anyString());
	}

	
	@Test
	public void testConfigureNoWikiPageNotEmbedded(){
		//if page is not embedded in the owner page, and the user can't edit, then it should show a 404
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		WikiPageWidget.Callback mockCallback = Mockito.mock(WikiPageWidget.Callback.class);
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), false, mockCallback, false);
		verify(mockView).show404();
		verify(mockCallback).noWikiFound();
	}
	
	@Test
	public void testConfigureWikiForbiddenNotEmbedded(){
		AsyncMockStubber.callFailureWith(new ForbiddenException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), false, null, false);
		verify(mockView).show403();
	}
	
	//also show a 404 if we get an empty entity list
	@Test
	public void testEmptyEntityList() throws JSONObjectAdapterException {
		BatchResults<EntityHeader> headers = new BatchResults<EntityHeader>();
		headers.setTotalNumberOfResults(0);
		EntityHeader testEntityHeader = new EntityHeader();
		testEntityHeader.setName(MY_TEST_ENTITY_OWNER_NAME);
		headers.setResults(new ArrayList());
		when(mockNodeModelCreator.createBatchResults(anyString(), any(Class.class))).thenReturn(headers);
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), false, null, true);
		
		verify(mockView).show404();
	}
	
	@Test
	public void testConfigureOtherErrorGettingWikiPage(){
		AsyncMockStubber.callFailureWith(new RuntimeException("another error")).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		verify(mockView).showWarningMessageInPage(anyString());
	}
	
	@Test
	public void testAddAttachments() throws IOException, RestServiceException, JSONObjectAdapterException{		
		String fileHandleId3 = "46";
		
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		
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
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		List<String> deleteHandleIds = new ArrayList<String>();
		deleteHandleIds.add(fileHandleId2);
		
		presenter.removeFileHandles(deleteHandleIds);
		List<String> currentFileHandleIds = presenter.getWikiPage().getAttachmentFileHandleIds();
		assertTrue(currentFileHandleIds.size() == 1);
		assertTrue(currentFileHandleIds.contains(fileHandleId1));
	}
	
	@Test
	public void testCreatePage() throws JSONObjectAdapterException{
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		presenter.createPage("a new wiki page with this title");
		verify(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testCreatePageWithCallback() throws JSONObjectAdapterException{
		org.sagebionetworks.web.client.utils.Callback onSuccessCallback =  mock(org.sagebionetworks.web.client.utils.Callback.class);
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		presenter.createPage("a new wiki page with this title", onSuccessCallback);
		verify(onSuccessCallback).invoke();
	}

	
	@Test
	public void testCreatePageFailure() throws JSONObjectAdapterException{		
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		AsyncMockStubber.callFailureWith(new RuntimeException("creation failed")).when(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class), any(AsyncCallback.class));
		presenter.createPage("a new wiki page with this title");
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_PAGE_CREATION_FAILED);
	}

	@Test
	public void testCancelClicked(){
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		presenter.cancelClicked();
		verify(mockGlobalApplicationState).setIsEditing(eq(false));
	}

	@Test
	public void testSaveClicked() throws JSONObjectAdapterException{
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		presenter.saveClicked("", "");
		verify(mockGlobalApplicationState).setIsEditing(eq(false));
	}
	
	@Test
	public void testSaveFailure() throws JSONObjectAdapterException{
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).updateV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class), any(AsyncCallback.class));
		presenter.saveClicked("", "");
		
		verify(mockGlobalApplicationState, Mockito.times(0)).setIsEditing(anyBoolean());
	}
	
	@Test
	public void testEditClicked(){
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		presenter.editClicked();
		verify(mockGlobalApplicationState).setIsEditing(eq(true));
	}
	
	@Test
	public void testShowCreatedBy(){
		presenter.showCreatedBy(false);
		verify(mockView).showCreatedBy(false);
	}
	
	@Test
	public void testShowModifiedBy(){
		presenter.showModifiedBy(false);
		verify(mockView).showModifiedBy(false);
	}
	
	@Test
	public void testShowWikiHistory(){
		presenter.showWikiHistory(false);
		verify(mockView).showWikiHistory(false);
	}

	
}
