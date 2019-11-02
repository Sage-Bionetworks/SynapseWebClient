package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.ClientProperties.DIFF_LIB_JS;
import static org.sagebionetworks.web.client.place.WikiDiff.OWNER_ID;
import static org.sagebionetworks.web.client.place.WikiDiff.OWNER_TYPE;
import static org.sagebionetworks.web.client.place.WikiDiff.WIKI_ID;
import static org.sagebionetworks.web.client.place.WikiDiff.WIKI_VERSION_1;
import static org.sagebionetworks.web.client.place.WikiDiff.WIKI_VERSION_2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.WikiDiff;
import org.sagebionetworks.web.client.presenter.WikiDiffPresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.view.WikiDiffView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class WikiDiffPresenterTest {
	WikiDiffPresenter presenter;
	@Mock
	WikiDiffView mockView;
	@Mock
	WikiDiff mockPlace;

	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	ResourceLoader mockResourceLoader;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Captor
	ArgumentCaptor<WikiPageKey> wikiPageKeyCaptor;
	@Mock
	PaginatedResults<V2WikiHistorySnapshot> mockSingleItemHistoryPaginatedResults;
	@Mock
	PaginatedResults<V2WikiHistorySnapshot> mockEmptyItemHistoryPaginatedResults;

	List<V2WikiHistorySnapshot> singleItemHistory;
	@Mock
	V2WikiHistorySnapshot mockV2WikiHistorySnapshot;
	List<V2WikiHistorySnapshot> emptyItemHistory = new ArrayList<>();

	@Mock
	WikiPage mockWikiPageV1;
	@Mock
	WikiPage mockWikiPageV2;

	public static final String OWNER_ID_VALUE = "syn29382";
	public static final String OWNER_TYPE_VALUE = "ENTITY";
	public static final String WIKI_PAGE_ID_VALUE = "8372";

	public static final Long VERSION_1_VALUE = 150L;
	public static final Long VERSION_2_VALUE = 152L;
	public static final String WIKI_MARKDOWN_VERSION_1 = "1 is the 1";
	public static final String WIKI_MARKDOWN_VERSION_2 = "2 is the 2";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		singleItemHistory = Collections.singletonList(mockV2WikiHistorySnapshot);
		when(mockSingleItemHistoryPaginatedResults.getResults()).thenReturn(singleItemHistory);
		when(mockEmptyItemHistoryPaginatedResults.getResults()).thenReturn(emptyItemHistory);
		when(mockResourceLoader.isLoaded(DIFF_LIB_JS)).thenReturn(false);
		when(mockPlace.getParam(OWNER_ID)).thenReturn(OWNER_ID_VALUE);
		when(mockPlace.getParam(OWNER_TYPE)).thenReturn(OWNER_TYPE_VALUE);
		when(mockPlace.getParam(WIKI_ID)).thenReturn(WIKI_PAGE_ID_VALUE);
		when(mockWikiPageV1.getMarkdown()).thenReturn(WIKI_MARKDOWN_VERSION_1);
		when(mockWikiPageV2.getMarkdown()).thenReturn(WIKI_MARKDOWN_VERSION_2);
		presenter = new WikiDiffPresenter(mockView, mockSynapseClient, mockJsClient, mockSynAlert, mockGlobalAppState, mockResourceLoader, mockJsniUtils);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(presenter);
		verify(mockView).setSynAlert(any(IsWidget.class));
		verify(mockResourceLoader).isLoaded(DIFF_LIB_JS);
		verify(mockResourceLoader).requires(anyList(), any(AsyncCallback.class));

		// also verify attempt to load diff library (simulate failure, since success is a no-op).
		Exception loadingErrorEx = new Exception();
		AsyncMockStubber.callFailureWith(loadingErrorEx).when(mockResourceLoader).requires(anyList(), any(AsyncCallback.class));
		presenter = new WikiDiffPresenter(mockView, mockSynapseClient, mockJsClient, mockSynAlert, mockGlobalAppState, mockResourceLoader, mockJsniUtils);
		verify(mockSynAlert).handleException(loadingErrorEx);
	}

	@Test
	public void testGetWikiHistory() {
		// versions are not set in the place (in this test)
		// simulate 2 pages are returned. first page contains an item, second page contains no items
		AsyncMockStubber.callSuccessWith(mockSingleItemHistoryPaginatedResults, mockEmptyItemHistoryPaginatedResults).when(mockSynapseClient).getV2WikiHistory(any(WikiPageKey.class), anyLong(), anyLong(), any(AsyncCallback.class));

		presenter.setPlace(mockPlace);

		verify(mockSynAlert).clear();
		verify(mockGlobalAppState).pushCurrentPlace(mockPlace);
		verify(mockView).setVersion1(null);
		verify(mockView).setVersion2(null);
		// verify it is called twice. verify wiki page key values
		verify(mockSynapseClient).getV2WikiHistory(wikiPageKeyCaptor.capture(), eq(WikiDiffPresenter.LIMIT), eq(0L), any(AsyncCallback.class));
		WikiPageKey key = wikiPageKeyCaptor.getValue();
		assertEquals(OWNER_ID_VALUE, key.getOwnerObjectId());
		assertEquals(OWNER_TYPE_VALUE, key.getOwnerObjectType());
		assertEquals(WIKI_PAGE_ID_VALUE, key.getWikiPageId());
		verify(mockSynapseClient).getV2WikiHistory(any(WikiPageKey.class), eq(WikiDiffPresenter.LIMIT), eq(WikiDiffPresenter.LIMIT), any(AsyncCallback.class));
		verify(mockView).setVersionHistory(anyList());
	}

	@Test
	public void testGetWikiHistoryFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getV2WikiHistory(any(WikiPageKey.class), anyLong(), anyLong(), any(AsyncCallback.class));

		presenter.setPlace(mockPlace);

		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testGetWikiMarkdown() {
		AsyncMockStubber.callSuccessWith(mockWikiPageV1).when(mockJsClient).getVersionOfV2WikiPageAsV1(any(WikiPageKey.class), eq(VERSION_1_VALUE), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockWikiPageV2).when(mockJsClient).getVersionOfV2WikiPageAsV1(any(WikiPageKey.class), eq(VERSION_2_VALUE), any(AsyncCallback.class));

		presenter.setPlace(mockPlace);

		verify(mockJsClient, never()).getVersionOfV2WikiPageAsV1(any(WikiPageKey.class), anyLong(), any(AsyncCallback.class));

		// simulate version 1 set
		presenter.onVersion1Selected(VERSION_1_VALUE.toString());

		verify(mockPlace).putParam(WIKI_VERSION_1, VERSION_1_VALUE.toString());
		verify(mockJsClient, never()).getVersionOfV2WikiPageAsV1(any(WikiPageKey.class), anyLong(), any(AsyncCallback.class));

		// simulate version 2 set
		presenter.onVersion2Selected(VERSION_2_VALUE.toString());
		verify(mockPlace).putParam(WIKI_VERSION_2, VERSION_2_VALUE.toString());
		verify(mockJsClient).getVersionOfV2WikiPageAsV1(any(WikiPageKey.class), eq(VERSION_1_VALUE), any(AsyncCallback.class));
		verify(mockJsClient).getVersionOfV2WikiPageAsV1(any(WikiPageKey.class), eq(VERSION_2_VALUE), any(AsyncCallback.class));
		verify(mockView).showDiff(WIKI_MARKDOWN_VERSION_1, WIKI_MARKDOWN_VERSION_2);
	}
}
