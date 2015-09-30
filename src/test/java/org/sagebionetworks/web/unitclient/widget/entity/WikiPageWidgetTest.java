package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Unit test for the preview widget.
 * @author jayhodgson
 *
 */
public class WikiPageWidgetTest {
		
	WikiPageWidgetView mockView;
	SynapseClientAsync mockSynapseClient;
	WikiPageWidget presenter;
	SynapseAlert mockSynapseAlert;
	WikiHistoryWidget mockHistoryWidget;
	MarkdownWidget mockMarkdownWidget;
	Breadcrumb mockBreadcrumb;
	WikiSubpagesWidget mockSubpages;
	UserBadge mockUserBadge;
	PortalGinInjector mockInjector;

	CallbackP<String> mockCallbackP;
	WikiPage testPage;
	WikiPageKey testPageKey;
	private static final String MY_TEST_ENTITY_OWNER_NAME = "My Test Entity Owner Name";
	
	@Before
	public void before() throws Exception{
		mockView = mock(WikiPageWidgetView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSynapseAlert = mock(SynapseAlert.class);
		mockHistoryWidget = mock(WikiHistoryWidget.class);
		mockBreadcrumb = mock(Breadcrumb.class);
		mockSubpages = mock(WikiSubpagesWidget.class);
		mockMarkdownWidget = mock(MarkdownWidget.class);
		mockInjector = mock(PortalGinInjector.class);
		mockUserBadge = mock(UserBadge.class);
		mockCallbackP = mock(CallbackP.class);
		
		when(mockInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		presenter = new WikiPageWidget(mockView, mockSynapseClient, mockSynapseAlert, mockHistoryWidget, mockMarkdownWidget,
				mockBreadcrumb, mockSubpages, mockInjector);
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
		boolean showSubpages = true;
		boolean canEdit = true;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, showSubpages, suffix);
		verify(mockView).setLoadingVisible(true);
		verify(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockMarkdownWidget).configure(anyString(), any(WikiPageKey.class), eq(suffix), any(Long.class));
		verify(mockBreadcrumb, never()).configure(anyList(), anyString());
		verify(mockHistoryWidget).configure(any(WikiPageKey.class), anyBoolean(), any(ActionHandler.class));
		verify(mockView, times(2)).setWikiHistoryWidget(any(IsWidget.class));
		verify(mockView).setWikiSubpagesContainers(any(WikiSubpagesWidget.class));
		verify(mockSubpages).configure(any(WikiPageKey.class), any(Callback.class), anyBoolean(), any(CallbackP.class));
		verify(mockView).setWikiSubpagesWidget(mockSubpages);
		verify(mockUserBadge, times(2)).configure(anyString());
		// once to clear, once after loading shown
		verify(mockView, times(2)).setLoadingVisible(false);
	}
	
	@Test
	public void testConfigureNoWikiPageNotFoundIsEmbedded(){
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean showSubpages = true;
		boolean canEdit = false;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, showSubpages, suffix);
		
		verify(mockSynapseAlert, never()).handleException(any(Exception.class));
		verify(mockView).setMarkdownVisible(false);
		verify(mockView).setWikiHistoryVisible(false);
		verify(mockView).setCreatedModifiedVisible(false);
		verify(mockView).setNoWikiCannotEditMessageVisible(true);
	}
	
	@Test
	public void testConfigureNoWikiPageNotFoundCanEditIsEmbedded(){
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean showSubpages = true;
		boolean canEdit = true;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, showSubpages, suffix);
		
		verify(mockSynapseAlert, never()).handleException(any(Exception.class));
		verify(mockView).setMarkdownVisible(false);
		verify(mockView).setWikiHistoryVisible(false);
		verify(mockView).setCreatedModifiedVisible(false);
		verify(mockView).setNoWikiCanEditMessageVisible(true);
	}

	@Test
	public void testConfigureNoWikiPageErrorIsEmbedded(){
		AsyncMockStubber.callFailureWith(new BadRequestException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean canEdit = false;
		boolean showSubpages = true;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, showSubpages, suffix);
		verify(mockSynapseAlert).handleException(any(Exception.class));
	}
	
	@Test
	public void testConfigureNoWikiPageNotEmbedded(){
		//if page is not embedded in the owner page, and the user can't edit, then it should show a 404
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		WikiPageWidget.Callback mockCallback = Mockito.mock(WikiPageWidget.Callback.class);
		boolean showSubpages = false;
		boolean canEdit = false;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, mockCallback, showSubpages, suffix);
		verify(mockSynapseAlert).show404();
		verify(mockCallback).noWikiFound();
	}
	
	@Test
	public void testConfigureNoWikiPageEmbeddedCanEdit(){
		AsyncMockStubber.callFailureWith(new BadRequestException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		WikiPageWidget.Callback mockCallback = Mockito.mock(WikiPageWidget.Callback.class);
		boolean showSubpages = true;
		boolean canEdit = true;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, mockCallback, showSubpages, suffix);
		verify(mockSynapseAlert).handleException(any(Exception.class));
	}
	
	@Test
	public void testConfigureWikiForbiddenNotEmbedded(){
		AsyncMockStubber.callFailureWith(new ForbiddenException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean showSubpages = false;
		boolean canEdit = false;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, showSubpages, suffix);
		verify(mockSynapseAlert).show403();
	}
	
	//also show a 404 if we get an empty entity list
	@Test
	public void testEmptyEntityList() throws JSONObjectAdapterException {
		boolean showSubpages = false;
		boolean canEdit = false;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, showSubpages, suffix);
		PaginatedResults<EntityHeader> headers = new PaginatedResults<EntityHeader>();
		headers.setTotalNumberOfResults(0);
		AsyncMockStubber.callSuccessWith(headers).when(mockSynapseClient).getEntityHeaderBatch(any(ReferenceList.class), any(AsyncCallback.class));
		presenter.setOwnerObjectName(mockCallbackP);
		verify(mockSynapseAlert).show404();
	}
	
	@Test
	public void testConfigureOtherErrorGettingWikiPage(){
		AsyncMockStubber.callFailureWith(new RuntimeException("another error")).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean showSubpages = true;
		boolean canEdit = true;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, showSubpages, suffix);
		verify(mockSynapseAlert).handleException(any(Exception.class));
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
		verify(mockView).setWikiHistoryVisible(false);
	}
	
	@Test
	public void testClear() {
		presenter.clear();
		verify(mockView).clear();
		verify(mockView).setLoadingVisible(false);
		verify(mockMarkdownWidget).clear();
		verify(mockBreadcrumb).clear();
		verify(mockSubpages).clearState();
		verify(mockView).setCreatedModifiedVisible(false);
		verify(mockView).setWikiHeadingText("");
	}

	@Test
	public void testReloadWikiPageSuccess() {
		presenter.setWikiReloadHandler(mockCallbackP);
		presenter.setCanEdit(true);
		WikiPageKey wikiPageKey = new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), "123", 1L);
		WikiPage wikiPage = new WikiPage();
		wikiPage.setId(wikiPageKey.getWikiPageId());
		presenter.setWikiPageKey(wikiPageKey);
		AsyncMockStubber.callSuccessWith(wikiPage).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.setWikiReloadHandler(mockCallbackP);
		presenter.reloadWikiPage();
		verify(mockSynapseAlert).clear();
		verify(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockView).setDiffVersionAlertVisible(false);
		verify(mockCallbackP).invoke(anyString());
		//also verify that the created by and modified by are updated when wiki page is reloaded
		verify(mockView).setCreatedModifiedVisible(true);
		verify(mockUserBadge, times(2)).configure(anyString());
	}

	@Test
	public void testReloadWikiPageFailure() {
		boolean showSubpages = true;
		boolean canEdit = false;
		String suffix = "-test-suffix";
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null, showSubpages, suffix);
		
		// fail to reload wiki page
		AsyncMockStubber.callFailureWith(new BadRequestException()).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.reloadWikiPage();
		verify(mockSynapseAlert).handleException(any(Exception.class));
	}
	
	@Test
	public void testConfigureCreatedModifiedBy() {
		WikiPage wikiPage = new WikiPage();
		presenter.setCurrentPage(wikiPage);
		presenter.configureCreatedModifiedBy();
		verify(mockUserBadge, times(2)).configure(anyString());
	}
	
	@Test
	public void testConfigureBreadcrumbsEntityObjectType() {
		WikiPage wikiPage = new WikiPage();
		wikiPage.setTitle("testTitle");
		WikiPageKey wikiPageKey = new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null);
		wikiPage.setId(wikiPageKey.getWikiPageId());
		presenter.setWikiPageKey(wikiPageKey);
		presenter.setCurrentPage(wikiPage);
		presenter.configureBreadcrumbs(false, ObjectType.ENTITY.toString());
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(mockBreadcrumb).configure(captor.capture(), Mockito.eq("testTitle"));
		LinkData data = (LinkData)captor.getValue().get(0);
		assertTrue(data.getPlace() instanceof Synapse);
	}

}
