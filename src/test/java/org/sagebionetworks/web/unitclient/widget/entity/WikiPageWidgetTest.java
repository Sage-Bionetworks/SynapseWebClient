package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WikiSubpagesCollapseEvent;
import org.sagebionetworks.web.client.events.WikiSubpagesExpandEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.binder.EventBinder;

/**
 * Unit test for the preview widget.
 * 
 * @author jayhodgson
 *
 */
public class WikiPageWidgetTest {
	@Mock
	WikiPageWidgetView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	StuAlert mockStuAlert;
	@Mock
	WikiHistoryWidget mockHistoryWidget;
	@Mock
	MarkdownWidget mockMarkdownWidget;
	@Mock
	WikiSubpagesWidget mockSubpages;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	PortalGinInjector mockInjector;
	@Mock
	CallbackP<String> mockCallbackP;
	@Mock
	SessionStorage mockSessionStorage;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Captor
	ArgumentCaptor<CallbackP<WikiPageKey>> callbackPCaptor;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	CookieProvider mockCookies;
	@Mock
	EventBus mockEventBus;
	@Mock
	EventBinder<WikiPageWidget> mockEventBinder;
	@Mock
	WikiSubpagesCollapseEvent mockWikiSubpagesCollapseEvent;
	@Mock
	WikiSubpagesExpandEvent mockWikiSubpagesExpandEvent;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();

	WikiPageWidget presenter;
	WikiPage testPage;
	public static final String WIKI_PAGE_ID = "12345";

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockView.getEventBinder()).thenReturn(mockEventBinder);
		presenter = new WikiPageWidget(mockView, mockSynapseClient, mockStuAlert, mockHistoryWidget, mockMarkdownWidget, mockSubpages, mockInjector, mockSessionStorage, mockAuthController, adapterFactory, mockDateTimeUtils, mockSynapseJavascriptClient, mockCookies, mockEventBus);
		testPage = new WikiPage();
		testPage.setId(WIKI_PAGE_ID);
		testPage.setMarkdown("my test markdown");
		testPage.setTitle("My Test Wiki Title");
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		WikiPage fakeWiki = new WikiPage();
		fakeWiki.setMarkdown("Fake wiki");
		AsyncMockStubber.callSuccessWith(fakeWiki).when(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class), any(AsyncCallback.class));
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
	}

	@Test
	public void testAsWidget() {
		presenter.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigure() throws JSONObjectAdapterException {
		boolean canEdit = true;
		String suffix = "-test-suffix";
		String formattedDate = "today";
		when(mockDateTimeUtils.getDateTimeString(any(Date.class))).thenReturn(formattedDate);
		WikiPageKey key = new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, null);
		presenter.configure(key, canEdit, null);
		verify(mockView).setLoadingVisible(true);
		verify(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockMarkdownWidget).configure(anyString(), any(WikiPageKey.class), any(Long.class));
		verify(mockView).setNoWikiCanEditMessageVisible(false);
		verify(mockView).setNoWikiCannotEditMessageVisible(false);
		verify(mockView).setWikiHistoryWidget(any(IsWidget.class));
		verify(mockView).setModifiedCreatedByHistoryPanelVisible(true);
		verify(mockView).setModifiedOn(formattedDate);
		verify(mockView).setCreatedOn(formattedDate);
		// once to clear, once after loading shown
		verify(mockView, times(2)).setLoadingVisible(false);
		verify(mockView).scrollWikiHeadingIntoView();
		verify(mockView).setWikiHistoryVisible(false);
		verify(mockHistoryWidget).clear();

		presenter.showSubpages(mockActionMenuWidget);
		verify(mockView).setWikiSubpagesWidget(mockSubpages);
		verify(mockSubpages).configure(any(WikiPageKey.class), anyBoolean(), any(CallbackP.class), any(ActionMenuWidget.class));

		verify(mockHistoryWidget, never()).configure(any(WikiPageKey.class), anyBoolean(), any(ActionHandler.class));
		presenter.showWikiHistory();
		verify(mockHistoryWidget).configure(any(WikiPageKey.class), anyBoolean(), any(ActionHandler.class));
		// in alpha mode, so show diff tool button
		verify(mockView).setWikiHistoryDiffToolButtonVisible(true, key);
	}

	// TODO: remove if exposing this outside of alpha mode
	@Test
	public void testDiffToolHiddenInNormalMode() {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn(null);
		WikiPageKey key = new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, null);
		presenter.configure(key, false, null);
		verify(mockView).setWikiHistoryDiffToolButtonVisible(false, key);
	}

	@Test
	public void testConfigureNoWikiPageNotFound() {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean canEdit = false;
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null);

		verify(mockStuAlert, never()).handleException(any(Exception.class));
		verify(mockView, times(2)).setMarkdownVisible(false);
		verify(mockView).setWikiHistoryVisible(false);
		verify(mockView).setNoWikiCannotEditMessageVisible(true);
		verify(mockView).setModifiedCreatedByHistoryPanelVisible(false);
	}

	@Test
	public void testConfigureNoWikiPageNotFoundCanEdit() {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean canEdit = true;
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null);

		verify(mockStuAlert, never()).handleException(any(Exception.class));
		verify(mockView, times(2)).setMarkdownVisible(false);
		verify(mockView).setWikiHistoryVisible(false);
		verify(mockView).setNoWikiCanEditMessageVisible(true);
		verify(mockView).setModifiedCreatedByHistoryPanelVisible(false);
	}

	@Test
	public void testConfigureNoWikiPageError() {
		AsyncMockStubber.callFailureWith(new BadRequestException()).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean canEdit = false;
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null);
		verify(mockStuAlert).handleException(any(Exception.class));
		verify(mockView).setMainPanelVisible(false);
		verify(mockView).setModifiedCreatedByHistoryPanelVisible(false);
	}

	@Test
	public void testConfigureOtherErrorGettingWikiPage() {
		AsyncMockStubber.callFailureWith(new RuntimeException("another error")).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		boolean canEdit = true;
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), canEdit, null);
		verify(mockStuAlert).handleException(any(Exception.class));
		verify(mockView).setMainPanelVisible(false);
		verify(mockView).setModifiedCreatedByHistoryPanelVisible(false);
	}

	@Test
	public void testShowWikiHistory() {
		boolean canEdit = true;
		presenter.setModifiedCreatedByHistoryVisible(false);
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, null), canEdit, null);
		verify(mockView).setModifiedCreatedByHistoryPanelVisible(false);
	}

	@Test
	public void testClear() {
		presenter.clear();
		verify(mockView).clear();
		verify(mockView).setLoadingVisible(false);
		verify(mockMarkdownWidget).clear();
		verify(mockView).setWikiSubpagesWidgetVisible(false);
		verify(mockView).setWikiHeadingText("");
	}

	@Test
	public void testReloadWikiPageSuccess() {
		boolean canEdit = true;
		presenter.setWikiReloadHandler(mockCallbackP);
		WikiPageKey wikiPageKey = new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, 1L);
		WikiPage wikiPage = new WikiPage();
		wikiPage.setId(wikiPageKey.getWikiPageId());
		presenter.setWikiPageKey(wikiPageKey);
		AsyncMockStubber.callSuccessWith(wikiPage).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));

		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, null), canEdit, null);
		verify(mockStuAlert, times(2)).clear();
		presenter.showSubpages(mockActionMenuWidget);
		verify(mockSubpages).configure(any(WikiPageKey.class), anyBoolean(), callbackPCaptor.capture(), any(ActionMenuWidget.class));
		// invoke subpage clicked
		callbackPCaptor.getValue().invoke(wikiPageKey);
		verify(mockSynapseJavascriptClient, times(2)).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockView, times(2)).setDiffVersionAlertVisible(false);
		verify(mockView, times(2)).scrollWikiHeadingIntoView();
		verify(mockCallbackP).invoke(anyString());
		// also verify that the created by and modified by are updated when wiki page is reloaded
		verify(mockView, times(2)).setModifiedOn(anyString());
		verify(mockView, times(2)).setCreatedOn(anyString());

		// verify response for a different wiki is ignored (if it does not match the current wiki page id,
		// then the markdown widget is not updated).
		reset(mockMarkdownWidget);
		wikiPage = new WikiPage();
		wikiPage.setId("different wiki page id");
		presenter.setWikiPage(wikiPage);
		verifyZeroInteractions(mockMarkdownWidget);
	}

	@Test
	public void testReloadWikiPageFailure() {
		boolean canEdit = false;
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, null), canEdit, null);

		// fail to reload wiki page
		AsyncMockStubber.callFailureWith(new BadRequestException()).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.reloadWikiPage();
		verify(mockStuAlert).handleException(any(Exception.class));
		verify(mockView).setMainPanelVisible(false);
		verify(mockView).scrollWikiHeadingIntoView();
		verify(mockView).setModifiedCreatedByHistoryPanelVisible(false);
	}

	@Test
	public void testWikiPageCached() throws JSONObjectAdapterException {
		String etag = "34567890987654";
		String md = "## markdown";
		WikiPage cachedWikiPage = new WikiPage();
		cachedWikiPage.setTitle("testTitle");
		cachedWikiPage.setMarkdown(md);
		WikiPageKey wikiPageKey = new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, null);
		cachedWikiPage.setId(wikiPageKey.getWikiPageId());
		cachedWikiPage.setEtag(etag);

		JSONObjectAdapter adapter = adapterFactory.createNew();
		cachedWikiPage.writeToJSONObject(adapter);
		when(mockSessionStorage.getItem(anyString())).thenReturn(adapter.toJSONString());

		V2WikiPage currentV2WikiPage = new V2WikiPage();
		currentV2WikiPage.setEtag(etag);
		AsyncMockStubber.callSuccessWith(currentV2WikiPage).when(mockSynapseJavascriptClient).getV2WikiPage(eq(wikiPageKey), any(AsyncCallback.class));

		presenter.configure(wikiPageKey, false, null);
		verify(mockSynapseJavascriptClient, never()).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockMarkdownWidget).configure(eq(md), eq(wikiPageKey), anyLong());
	}

	@Test
	public void testOutOfDateWikiPageCached() throws JSONObjectAdapterException {
		String oldEtag = "34567890987654";
		String newEtag = "34567890987655";
		String md = "## markdown";
		WikiPage cachedWikiPage = new WikiPage();
		cachedWikiPage.setTitle("testTitle");
		cachedWikiPage.setMarkdown(md);
		WikiPageKey wikiPageKey = new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, null);
		cachedWikiPage.setId(wikiPageKey.getWikiPageId());
		cachedWikiPage.setEtag(oldEtag);

		JSONObjectAdapter adapter = adapterFactory.createNew();
		cachedWikiPage.writeToJSONObject(adapter);
		when(mockSessionStorage.getItem(anyString())).thenReturn(adapter.toJSONString());

		V2WikiPage currentV2WikiPage = new V2WikiPage();
		currentV2WikiPage.setEtag(newEtag);
		currentV2WikiPage.setId(wikiPageKey.getWikiPageId());
		AsyncMockStubber.callSuccessWith(currentV2WikiPage).when(mockSynapseJavascriptClient).getV2WikiPage(eq(wikiPageKey), any(AsyncCallback.class));

		presenter.configure(wikiPageKey, false, null);
		verify(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockMarkdownWidget).configure(eq(testPage.getMarkdown()), eq(wikiPageKey), anyLong());
	}

	@Test
	public void testSessionCacheKey() {
		String principalId = "111111";
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(principalId);
		String ownerId = "34567654";
		String objectType = ObjectType.ENTITY.toString();
		String wikiId = "098765432";
		WikiPageKey wikiPageKey = new WikiPageKey(ownerId, objectType, wikiId);
		String cacheKey = presenter.getSessionCacheKey(wikiPageKey);
		assertTrue(cacheKey.contains(principalId));
		assertTrue(cacheKey.contains(ownerId));
		assertTrue(cacheKey.contains(objectType));
		assertTrue(cacheKey.contains(wikiId));
		assertTrue(cacheKey.contains(WebConstants.WIKIPAGE_SUFFIX));
	}

	@Test
	public void testCollapseWikiSubpages() {
		presenter.onWikiSubpagesCollapseEvent(mockWikiSubpagesCollapseEvent);
		verify(mockView).collapseWikiSubpages();
	}

	@Test
	public void testExpandWikiSubpages() {
		presenter.onWikiSubpagesExpandEvent(mockWikiSubpagesExpandEvent);
		verify(mockView).expandWikiSubpages();
	}

	@Test
	public void testSetWikiPageEmptyMarkdownCannotEdit() {
		boolean canEdit = false;
		testPage.setMarkdown("");

		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, null), canEdit, null);

		verify(mockView).setNoWikiCannotEditMessageVisible(true);
		verify(mockView).setMarkdownVisible(false);
	}

	@Test
	public void testSetWikiPageNullMarkdownCanEdit() {
		boolean canEdit = true;
		testPage.setMarkdown(null);

		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), WIKI_PAGE_ID, null), canEdit, null);

		verify(mockView).setNoWikiCanEditMessageVisible(true);
		verify(mockView).setMarkdownVisible(false);
	}
}
