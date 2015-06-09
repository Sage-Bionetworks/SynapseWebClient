package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
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
	WikiPageWidget presenter;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	SynapseAlert mockSynapseAlert;

	WikiPage testPage;
	private static final String MY_TEST_ENTITY_OWNER_NAME = "My Test Entity Owner Name";
	
	@Before
	public void before() throws Exception{
		mockView = mock(WikiPageWidgetView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseAlert = mock(SynapseAlert.class);
		presenter = new WikiPageWidget(mockView, mockSynapseClient,
				mockGlobalApplicationState, mockAuthenticationController, mockSynapseAlert);
		PaginatedResults<EntityHeader> headers = new PaginatedResults<EntityHeader>();
		headers.setTotalNumberOfResults(1);
		List<EntityHeader> resultHeaderList = new ArrayList<EntityHeader>();
		EntityHeader testEntityHeader = new EntityHeader();
		testEntityHeader.setName(MY_TEST_ENTITY_OWNER_NAME);
		testEntityHeader.setType(Project.class.getName());
		resultHeaderList.add(testEntityHeader);
		headers.setResults(resultHeaderList);
		AsyncMockStubber.callSuccessWith(headers).when(mockSynapseClient).getEntityHeaderBatch(any(ReferenceList.class), any(AsyncCallback.class));
		
		testPage = new WikiPage();
		testPage.setId("wikiPageId");
		testPage.setMarkdown("my test markdown");
		testPage.setTitle("My Test Wiki Title");
		
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
		verify(mockView).configure(anyString(), any(WikiPageKey.class), anyString(), anyBoolean(), anyBoolean(), eq(true), any(Long.class), eq(true));
	}
	
	@Test
	public void testConfigureNoWikiPageNotFoundIsEmbedded(){
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean canEdit = false;
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, true);
		verify(mockSynapseAlert, never()).handleException(any(Exception.class));
		verify(mockView).showNoteInPage(DisplayConstants.NO_WIKI_FOUND);
	}
	
	@Test
	public void testConfigureNoWikiPageNotFoundCanEditIsEmbedded(){
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean canEdit = true;
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, true);
		verify(mockSynapseAlert, never()).handleException(any(Exception.class));
		verify(mockView).showNoteInPage(DisplayConstants.LABEL_NO_MARKDOWN);
	}

	@Test
	public void testConfigureNoWikiPageErrorIsEmbedded(){
		AsyncMockStubber.callFailureWith(new BadRequestException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean canEdit = false;
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, true);
		verify(mockSynapseAlert).handleException(any(Exception.class));
		verify(mockView).showSynapseAlertWidget();
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
	public void testConfigureNoWikiPageEmbeddedCanEdit(){
		AsyncMockStubber.callFailureWith(new BadRequestException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		WikiPageWidget.Callback mockCallback = Mockito.mock(WikiPageWidget.Callback.class);
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, mockCallback, true);
		verify(mockSynapseAlert).handleException(any(Exception.class));
		verify(mockView).showSynapseAlertWidget();
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
		PaginatedResults<EntityHeader> headers = new PaginatedResults<EntityHeader>();
		headers.setTotalNumberOfResults(0);
		EntityHeader testEntityHeader = new EntityHeader();
		testEntityHeader.setName(MY_TEST_ENTITY_OWNER_NAME);
		headers.setResults(new ArrayList());
		AsyncMockStubber.callSuccessWith(headers).when(mockSynapseClient).getEntityHeaderBatch(any(ReferenceList.class), any(AsyncCallback.class));
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), false, null, true);
		verify(mockView).show404();
	}
	
	@Test
	public void testConfigureOtherErrorGettingWikiPage(){
		AsyncMockStubber.callFailureWith(new RuntimeException("another error")).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		verify(mockSynapseAlert).handleException(any(Exception.class));
		verify(mockView).showSynapseAlertWidget();
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

	@Test
	public void testReloadWikiPageSuccess() {
		CallbackP<String> mockWikiReloadHandler = mock(CallbackP.class);
		presenter.setWikiReloadHandler(mockWikiReloadHandler);
		WikiPageKey wikiPageKey = new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null);
		WikiPage wikiPage = new WikiPage();
		wikiPage.setId(wikiPageKey.getWikiPageId());
		AsyncMockStubber.callSuccessWith(wikiPage).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, null, true);
		presenter.reloadWikiPage();
		verify(mockView).resetWikiMarkdown(anyString(), eq(wikiPageKey), anyBoolean(), anyBoolean(), any(Long.class));
		verify(mockWikiReloadHandler).invoke(anyString());
	}

	@Test
	public void testReloadWikiPageFailure() {
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), false, null, true);
		// fail to reload wiki page
		AsyncMockStubber.callFailureWith(new BadRequestException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.reloadWikiPage();
		verify(mockSynapseAlert).handleException(any(Exception.class));
		verify(mockView).showSynapseAlertWidget();
	}
}
