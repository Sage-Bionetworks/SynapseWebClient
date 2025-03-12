package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyRange;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.analytics.SearchAnalyticsClient;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchQueryEventData;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchResultEventData;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchResultPageReturnedEventData;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SearchUtil;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.SearchQueryUtils;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SearchPresenterTest {

  SearchPresenter searchPresenter;

  @Mock
  SearchView mockView;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  AuthenticationController mockAuthenticationController;

  @Mock
  SynapseJavascriptClient mockJsClient;

  JSONObjectAdapter jsonObjectAdapter;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  LoadMoreWidgetContainer mockLoadMoreWidgetContainer;

  @Mock
  SearchAnalyticsClient mockSearchAnalyticsClient;

  @Mock
  SynapseJSNIUtils mockJsniUtils;

  @Mock
  ClickEvent mockClickEvent;

  @Captor
  ArgumentCaptor<Callback> loadMoreCallbackCaptor;

  @Captor
  ArgumentCaptor<AsyncCallback> asyncCallbackCaptor;

  @Captor
  ArgumentCaptor<SearchQueryEventData> searchQuerySubmittedEventDataCaptor;

  @Captor
  ArgumentCaptor<
    SearchResultPageReturnedEventData
  > searchResultPageReturnedEventDataCaptor;

  @Captor
  ArgumentCaptor<SearchResultEventData> searchResultEventDataArgumentCaptor;

  String exampleTerm;
  SearchQuery exampleTermSearchQuery;

  @Before
  public void setup() throws Exception {
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
    jsonObjectAdapter = new JSONObjectAdapterImpl();

    searchPresenter =
      new SearchPresenter(
        mockView,
        mockGlobalApplicationState,
        mockJsClient,
        new JSONObjectAdapterImpl(),
        mockSynAlert,
        mockLoadMoreWidgetContainer,
        mockSearchAnalyticsClient,
        mockJsniUtils
      );

    exampleTerm = "searchQueryTerm";
    exampleTermSearchQuery = SearchQueryUtils.getDefaultSearchQuery();
    exampleTermSearchQuery.setQueryTerm(
      Arrays.asList(new String[] { exampleTerm })
    );
  }

  private String getTermSearchQueryJson(SearchQuery query) throws Exception {
    return query
      .writeToJSONObject(jsonObjectAdapter.createNew())
      .toJSONString();
  }

  @Test
  public void constructor() {
    verify(mockView).setPresenter(searchPresenter);
  }

  @Test
  public void testWillRedirectEmptySearchTerm() throws Exception {
    assertNull(SearchUtil.willRedirect((String) null));
    assertNull(SearchUtil.willRedirect(""));
    assertNull(SearchUtil.willRedirect("   "));
  }

  @Test
  public void testSetPlace() {
    reset(mockView);
    // default, set presenter, null query
    Search place = Mockito.mock(Search.class);
    searchPresenter.setPlace(place);
    verify(mockView).setPresenter(searchPresenter);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testQueryTerm() throws Exception {
    searchPresenter.setPlace(new Search(exampleTerm));
    verify(mockJsClient)
      .getSearchResults(eq(exampleTermSearchQuery), any(AsyncCallback.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSearchQuery() throws Exception {
    // Use a smaller initial page size to simplify test setup
    exampleTermSearchQuery.setSize(2L);

    Search place = new Search(getTermSearchQueryJson(exampleTermSearchQuery));
    searchPresenter.setPlace(place);

    verify(mockJsClient)
      .getSearchResults(
        eq(exampleTermSearchQuery),
        asyncCallbackCaptor.capture()
      );

    verify(mockSearchAnalyticsClient)
      .sendSearchQuerySubmittedEvent(
        searchQuerySubmittedEventDataCaptor.capture()
      );

    SearchQueryEventData searchSubmittedEventData =
      searchQuerySubmittedEventDataCaptor.getValue();
    assertEquals("searchQueryTerm", searchSubmittedEventData.query_term);
    assertEquals("synapse_entity", searchSubmittedEventData.search_context);
    assertEquals(null, searchSubmittedEventData.serialized_boolean_query);
    assertEquals(null, searchSubmittedEventData.serialized_range_query);
    assertEquals(Double.valueOf(1), searchSubmittedEventData.page_index);
    assertEquals(Double.valueOf(0), searchSubmittedEventData.start_index);

    // Simulate first page of results
    List<Hit> hits = new ArrayList<Hit>();
    hits.add(new Hit());
    hits.get(0).setId("syn123");
    hits.get(0).setName("name1");
    hits.add(new Hit());
    hits.get(1).setId("syn456");
    hits.get(1).setName("name2");
    SearchResults searchResults = new SearchResults();
    searchResults.setFound(3L);
    searchResults.setStart(0L);
    searchResults.setHits(hits);
    asyncCallbackCaptor.getValue().onSuccess(searchResults);

    verify(mockView).setSearchResults(eq(searchResults), eq(exampleTerm));
    verify(mockSearchAnalyticsClient)
      .sendSearchResultPageReturnedEvent(
        searchResultPageReturnedEventDataCaptor.capture()
      );

    SearchResultPageReturnedEventData pageReturnedEventData =
      searchResultPageReturnedEventDataCaptor.getValue();

    assertEquals("searchQueryTerm", pageReturnedEventData.query_term);
    assertEquals("synapse_entity", pageReturnedEventData.search_context);
    assertNull(pageReturnedEventData.serialized_boolean_query);
    assertNull(pageReturnedEventData.serialized_range_query);
    assertEquals(Double.valueOf(1), pageReturnedEventData.page_index);
    assertEquals(Double.valueOf(0), pageReturnedEventData.start_index);
    assertEquals(Double.valueOf(3), pageReturnedEventData.total_results);

    verify(mockSearchAnalyticsClient, times(2))
      .sendSearchResultReturnedEvent(
        searchResultEventDataArgumentCaptor.capture()
      );

    List<SearchResultEventData> resultReturnedEventData =
      searchResultEventDataArgumentCaptor.getAllValues();

    resultReturnedEventData.forEach(eventData -> {
      assertEquals("searchQueryTerm", eventData.query_term);
      assertEquals("synapse_entity", eventData.search_context);
      assertNull(eventData.serialized_boolean_query);
      assertNull(eventData.serialized_range_query);

      assertEquals("entity", eventData.item_type);
      assertEquals(Double.valueOf(1), eventData.page_index);
    });

    assertEquals("syn123", resultReturnedEventData.get(0).item_id);
    assertEquals(Double.valueOf(1), resultReturnedEventData.get(0).rank);
    assertEquals("syn456", resultReturnedEventData.get(1).item_id);
    assertEquals(Double.valueOf(2), resultReturnedEventData.get(1).rank);

    // Request a second page of results
    verify(mockLoadMoreWidgetContainer)
      .configure(loadMoreCallbackCaptor.capture());
    loadMoreCallbackCaptor.getValue().invoke();

    exampleTermSearchQuery.setStart(2L);
    verify(mockJsClient, times(2))
      .getSearchResults(
        eq(exampleTermSearchQuery),
        asyncCallbackCaptor.capture()
      );

    verify(mockSearchAnalyticsClient, times(2))
      .sendSearchQuerySubmittedEvent(
        searchQuerySubmittedEventDataCaptor.capture()
      );

    searchSubmittedEventData = searchQuerySubmittedEventDataCaptor.getValue();
    assertEquals("searchQueryTerm", searchSubmittedEventData.query_term);
    assertEquals("synapse_entity", searchSubmittedEventData.search_context);
    assertEquals(null, searchSubmittedEventData.serialized_boolean_query);
    assertEquals(null, searchSubmittedEventData.serialized_range_query);
    assertEquals(Double.valueOf(2), searchSubmittedEventData.page_index);
    assertEquals(Double.valueOf(2), searchSubmittedEventData.start_index);

    // Second page of results is returned
    List<Hit> hitsPage2 = Collections.singletonList(new Hit());
    hitsPage2.get(0).setId("syn789");
    hitsPage2.get(0).setName("name3");
    SearchResults searchResultsPage2 = new SearchResults();
    searchResultsPage2.setFound(3L);
    searchResultsPage2.setStart(2L);
    searchResultsPage2.setHits(hitsPage2);

    asyncCallbackCaptor.getValue().onSuccess(searchResultsPage2);

    // Verify analytics events for the 2nd page were sent
    verify(mockSearchAnalyticsClient, times(2))
      .sendSearchResultPageReturnedEvent(
        searchResultPageReturnedEventDataCaptor.capture()
      );
    verify(mockSearchAnalyticsClient, times(3))
      .sendSearchResultReturnedEvent(
        searchResultEventDataArgumentCaptor.capture()
      );

    pageReturnedEventData = searchResultPageReturnedEventDataCaptor.getValue();

    assertEquals("searchQueryTerm", pageReturnedEventData.query_term);
    assertEquals("synapse_entity", pageReturnedEventData.search_context);
    assertNull(pageReturnedEventData.serialized_boolean_query);
    assertNull(pageReturnedEventData.serialized_range_query);
    assertEquals(Double.valueOf(2), pageReturnedEventData.page_index);
    assertEquals(Double.valueOf(2), pageReturnedEventData.start_index);
    assertEquals(Double.valueOf(3), pageReturnedEventData.total_results);

    SearchResultEventData lastResultReturnedEventData =
      searchResultEventDataArgumentCaptor.getValue();

    assertEquals("searchQueryTerm", lastResultReturnedEventData.query_term);
    assertEquals("synapse_entity", lastResultReturnedEventData.search_context);
    assertNull(lastResultReturnedEventData.serialized_boolean_query);
    assertNull(lastResultReturnedEventData.serialized_range_query);
    assertEquals("syn789", lastResultReturnedEventData.item_id);
    assertEquals(Double.valueOf(3), lastResultReturnedEventData.rank);
    assertEquals("entity", lastResultReturnedEventData.item_type);
    assertEquals(Double.valueOf(2), lastResultReturnedEventData.page_index);

    // Verify that clicking the results sends expected events

    searchPresenter
      .getSearchResultClickedHandler(hits.get(0))
      .onClick(mockClickEvent);

    verify(mockSearchAnalyticsClient)
      .sendSearchResultClickedEvent(
        searchResultEventDataArgumentCaptor.capture()
      );

    SearchResultEventData clickedEventData =
      searchResultEventDataArgumentCaptor.getValue();
    assertEquals("searchQueryTerm", clickedEventData.query_term);
    assertEquals("synapse_entity", clickedEventData.search_context);
    assertEquals("entity", clickedEventData.item_type);
    assertEquals("syn123", clickedEventData.item_id);
    assertEquals(Double.valueOf(1), clickedEventData.rank);
    assertEquals(Double.valueOf(1), clickedEventData.page_index);

    searchPresenter
      .getSearchResultClickedHandler(hitsPage2.get(0))
      .onClick(mockClickEvent);

    verify(mockSearchAnalyticsClient, times(2))
      .sendSearchResultClickedEvent(
        searchResultEventDataArgumentCaptor.capture()
      );

    clickedEventData = searchResultEventDataArgumentCaptor.getValue();
    assertEquals("searchQueryTerm", clickedEventData.query_term);
    assertEquals("synapse_entity", clickedEventData.search_context);
    assertEquals("entity", clickedEventData.item_type);
    assertEquals("syn789", clickedEventData.item_id);
    assertEquals(Double.valueOf(3), clickedEventData.rank);
    assertEquals(Double.valueOf(2), clickedEventData.page_index);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSearchQueryEmptyFirstPage() throws Exception {
    Search place = new Search(getTermSearchQueryJson(exampleTermSearchQuery));
    searchPresenter.setPlace(place);
    verify(mockJsClient)
      .getSearchResults(
        eq(exampleTermSearchQuery),
        asyncCallbackCaptor.capture()
      );

    verify(mockSearchAnalyticsClient)
      .sendSearchQuerySubmittedEvent(
        searchQuerySubmittedEventDataCaptor.capture()
      );

    SearchQueryEventData searchSubmittedEventData =
      searchQuerySubmittedEventDataCaptor.getValue();
    assertEquals("searchQueryTerm", searchSubmittedEventData.query_term);
    assertEquals("synapse_entity", searchSubmittedEventData.search_context);
    assertEquals(null, searchSubmittedEventData.serialized_boolean_query);
    assertEquals(null, searchSubmittedEventData.serialized_range_query);
    assertEquals(Double.valueOf(1), searchSubmittedEventData.page_index);
    assertEquals(Double.valueOf(0), searchSubmittedEventData.start_index);

    // Simulate first page of results
    SearchResults searchResults = new SearchResults();
    searchResults.setHits(new ArrayList<Hit>());
    searchResults.setFound(0L);
    searchResults.setStart(0L);
    asyncCallbackCaptor.getValue().onSuccess(searchResults);

    // Verify page return analytics event is sent, but there are no individual result events

    verify(mockSearchAnalyticsClient)
      .sendSearchResultPageReturnedEvent(
        searchResultPageReturnedEventDataCaptor.capture()
      );

    SearchResultPageReturnedEventData pageReturnedEventData =
      searchResultPageReturnedEventDataCaptor.getValue();

    assertEquals("searchQueryTerm", pageReturnedEventData.query_term);
    assertEquals("synapse_entity", pageReturnedEventData.search_context);
    assertNull(pageReturnedEventData.serialized_boolean_query);
    assertNull(pageReturnedEventData.serialized_range_query);
    assertEquals(Double.valueOf(1), pageReturnedEventData.page_index);
    assertEquals(Double.valueOf(0), pageReturnedEventData.start_index);
    assertEquals(Double.valueOf(0), pageReturnedEventData.total_results);

    verify(mockSearchAnalyticsClient, never())
      .sendSearchResultReturnedEvent(
        searchResultEventDataArgumentCaptor.capture()
      );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSetPlaceSynapseIdPrefixNotId() throws Exception {
    // test for a word with the prefix but not a synapse ID
    String term = ClientProperties.SYNAPSE_ID_PREFIX + "apse"; // # 'synapse'

    SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
    query.setQueryTerm(Arrays.asList(new String[] { term }));

    searchPresenter.setPlace(new Search(term));
    verify(mockJsClient).getSearchResults(eq(query), any(AsyncCallback.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSetPlaceSynapseIdPrefix() throws Exception {
    // test for a word with the prefix and is a synapse ID
    String term = ClientProperties.SYNAPSE_ID_PREFIX + "1234567890"; // # 'syn1234567890'
    assertEquals(new Synapse(term), SearchUtil.willRedirect(new Search(term)));
  }

  @Test
  public void testSetPlaceUsernamePrefix() throws Exception {
    String userName = "VaderLabTech";
    Place redirectPlace = SearchUtil.willRedirect("@" + userName);
    assertTrue(redirectPlace instanceof PeopleSearch);
    assertEquals(userName, ((PeopleSearch) redirectPlace).getSearchTerm());
  }

  private List<KeyValue> getFacet(String facetName) {
    List<KeyValue> facets = searchPresenter.getAppliedFacets();
    List<KeyValue> foundFacets = new ArrayList<KeyValue>();
    for (KeyValue facet : facets) {
      if (facetName.equals(facet.getKey())) {
        foundFacets.add(facet);
      }
    }
    return foundFacets;
  }

  private List<KeyRange> getTimeFacet(String facetName) {
    List<KeyRange> facets = searchPresenter.getAppliedTimeFacets();
    List<KeyRange> foundFacets = new ArrayList<KeyRange>();
    for (KeyRange facet : facets) {
      if (facetName.equals(facet.getKey())) {
        foundFacets.add(facet);
      }
    }
    return foundFacets;
  }

  @Test
  public void testTimeFacets() {
    String facetName = "createdOn";
    String facetValue = "1";
    assertTrue(getFacet(facetName).isEmpty());

    searchPresenter.addTimeFacet(facetName, facetValue, "Yesterday");

    // When a new facet is added, we should:
    // 1. Add the facet to the current search.
    // 2. Clear existing search results.
    // 3. Update the address bar to the new place.
    // 4. Execute the current search (that now has the new facet).

    // 1
    List<KeyRange> facetValues = getTimeFacet(facetName);
    assertEquals(1, facetValues.size());
    assertEquals(facetValue, facetValues.get(0).getMin());
    verify(mockView).clear(); // 2
    verify(mockLoadMoreWidgetContainer).clear(); // 2
    verify(mockGlobalApplicationState).pushCurrentPlace(any(Search.class)); // 3
    verify(mockLoadMoreWidgetContainer).setIsProcessing(true); // 4
    verify(mockJsClient)
      .getSearchResults(any(SearchQuery.class), any(AsyncCallback.class)); // 4
    verify(mockSearchAnalyticsClient)
      .sendSearchQuerySubmittedEvent(
        searchQuerySubmittedEventDataCaptor.capture()
      );
    SearchQueryEventData eventData =
      searchQuerySubmittedEventDataCaptor.getValue();
    assertEquals(
      "[{\"key\":\"createdOn\",\"min\":\"1\"}]",
      eventData.serialized_range_query
    );

    // verify setting the time facet to another value clears the previous
    facetValue = "2";
    searchPresenter.addTimeFacet(facetName, facetValue, "Yesterday");
    verify(mockJsClient, times(2))
      .getSearchResults(any(SearchQuery.class), any(AsyncCallback.class)); // 4
    facetValues = getTimeFacet(facetName);
    assertEquals(1, facetValues.size());
    assertEquals(facetValue, facetValues.get(0).getMin());

    searchPresenter.removeTimeFacetAndRefresh(facetName);
    // optimization. if search is empty (no search term and no facets) then a search is not performed
    // (empty results are shown).
    verify(mockJsClient, times(2))
      .getSearchResults(any(SearchQuery.class), any(AsyncCallback.class)); // 4
    assertTrue(getTimeFacet(facetName).isEmpty());

    verify(mockSearchAnalyticsClient, times(2))
      .sendSearchQuerySubmittedEvent(
        searchQuerySubmittedEventDataCaptor.capture()
      );
    eventData = searchQuerySubmittedEventDataCaptor.getValue();
    assertEquals(
      "[{\"key\":\"createdOn\",\"min\":\"2\"}]",
      eventData.serialized_range_query
    );
  }

  @Test
  public void testSetPlaceWithJsonSynapseIdAsSearchTerm() throws Exception {
    // verify searching on a single term that is a Synapse ID redirects to the Synapse place
    String term = ClientProperties.SYNAPSE_ID_PREFIX + "1234567890"; // # 'syn1234567890'

    SearchQuery searchQuery = SearchQueryUtils.getDefaultSearchQuery();
    searchQuery.setQueryTerm(Arrays.asList(new String[] { term }));
    String searchQueryJson = searchQuery
      .writeToJSONObject(jsonObjectAdapter.createNew())
      .toJSONString();
    searchPresenter.setPlace(new Search(searchQueryJson));

    verify(mockPlaceChanger).goTo(new Synapse(term));

    verify(mockSearchAnalyticsClient, never())
      .sendSearchQuerySubmittedEvent(any());
  }

  @Test
  public void testEncodedQueryTerm() throws Exception {
    searchPresenter.setPlace(new Search("Alzheimer's%20Disease"));
    SearchQuery expectedQuery = SearchQueryUtils.getDefaultSearchQuery();
    expectedQuery.setQueryTerm(
      Arrays.asList(new String[] { "Alzheimer's", "Disease" })
    );
    verify(mockJsClient)
      .getSearchResults(eq(expectedQuery), any(AsyncCallback.class));
  }

  @Test
  public void testGetSearchResultClickedHandler() throws Exception {
    Search place = new Search(getTermSearchQueryJson(exampleTermSearchQuery));
    searchPresenter.setPlace(place);

    Hit hit = new Hit();
    hit.setId("syn123");
    hit.setName("name1");
  }
}
