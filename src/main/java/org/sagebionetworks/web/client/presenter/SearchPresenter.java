package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyRange;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.analytics.SearchAnalyticsClient;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchContext;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchItemType;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchQueryEventData;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchResultEventData;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchResultPageReturnedEventData;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.SearchQueryUtils;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

public class SearchPresenter
  extends AbstractActivity
  implements SearchView.Presenter, Presenter<Search> {

  private final SearchView view;
  private final GlobalApplicationState globalApplicationState;
  private final JSONObjectAdapter jsonObjectAdapter;
  private final SynapseAlert synAlert;

  private SearchQuery currentSearch;
  private SearchResults currentResult;
  private final Map<String, String> timeValueToDisplay = new HashMap<
    String,
    String
  >();
  private Date searchStartTime;
  private final SynapseJavascriptClient jsClient;

  private final LoadMoreWidgetContainer loadMoreWidgetContainer;
  private final SearchAnalyticsClient searchAnalyticsClient;
  private final SynapseJSNIUtils jsniUtils;

  private final List<SearchResults> allPagesOfResults;

  @Inject
  public SearchPresenter(
    SearchView view,
    GlobalApplicationState globalApplicationState,
    SynapseJavascriptClient jsClient,
    JSONObjectAdapter jsonObjectAdapter,
    SynapseAlert synAlert,
    LoadMoreWidgetContainer loadMoreWidgetContainer,
    SearchAnalyticsClient searchAnalyticsClient,
    SynapseJSNIUtils jsniUtils
  ) {
    this.view = view;
    this.globalApplicationState = globalApplicationState;
    this.jsonObjectAdapter = jsonObjectAdapter;
    this.synAlert = synAlert;
    this.loadMoreWidgetContainer = loadMoreWidgetContainer;
    this.jsClient = jsClient;
    this.searchAnalyticsClient = searchAnalyticsClient;
    this.jsniUtils = jsniUtils;
    allPagesOfResults = new ArrayList<>();
    currentSearch = getBaseSearchQuery();
    view.setPresenter(this);
    view.setSynAlertWidget(synAlert.asWidget());
    loadMoreWidgetContainer.configure(
      new Callback() {
        @Override
        public void invoke() {
          executeSearch();
        }
      }
    );
    view.setLoadingMoreContainerWidget(loadMoreWidgetContainer.asWidget());
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    panel.setWidget(view);
  }

  @Override
  public void setPlace(Search place) {
    view.setPresenter(this);
    String queryTerm = place.getSearchTerm();
    if (queryTerm == null) queryTerm = "";
    currentSearch = checkForJson(queryTerm);
    if (currentSearch.getQueryTerm().size() == 1) {
      Place redirectPlace = SearchUtil.willRedirect(
        currentSearch.getQueryTerm().get(0)
      );
      if (redirectPlace != null) {
        globalApplicationState.getPlaceChanger().goTo(redirectPlace);
        return;
      }
    }
    if (place.getStart() != null) currentSearch.setStart(place.getStart());
    executeSearch();
  }

  @Override
  public String mayStop() {
    view.clear();
    loadMoreWidgetContainer.clear();
    return null;
  }

  @Override
  public void setSearchTerm(String queryTerm) {
    SearchUtil.searchForTerm(queryTerm, globalApplicationState);
  }

  @Override
  public void addFacet(String facetName, String facetValue) {
    List<KeyValue> bq = currentSearch.getBooleanQuery();
    if (bq == null) {
      bq = new ArrayList<KeyValue>();
      currentSearch.setBooleanQuery(bq);
    }

    // check if exists
    boolean exists = false;
    for (KeyValue kv : bq) {
      if (kv.getKey().equals(facetName) && kv.getValue().equals(facetValue)) {
        exists = true;
        break;
      }
    }

    // only add if not exists already. but do run the search
    if (!exists) {
      // add facet to query list
      KeyValue kv = new KeyValue();
      kv.setKey(facetName);
      kv.setValue(facetValue);
      bq.add(kv);
    }

    executeNewSearch();
  }

  private void executeNewSearch() {
    currentSearch.setStart(0L);
    view.clear();
    allPagesOfResults.clear();
    loadMoreWidgetContainer.clear();
    Search searchPlace = new Search(getCurrentSearchJSON());
    globalApplicationState.pushCurrentPlace(searchPlace);
    executeSearch();
  }

  @Override
  public void addTimeFacet(
    String facetName,
    String facetValue,
    String displayValue
  ) {
    timeValueToDisplay.put(
      createTimeValueKey(facetName, facetValue),
      displayValue
    );
    List<KeyRange> rq = currentSearch.getRangeQuery();
    if (rq == null) {
      rq = new ArrayList<>();
      currentSearch.setRangeQuery(rq);
    }
    KeyRange targetKeyRange = null;
    for (KeyRange keyRange : rq) {
      if (facetName.equals(keyRange.getKey())) {
        targetKeyRange = keyRange;
        if (keyRange.getMin().equals(facetValue)) {
          // no change, return
          return;
        }
        break;
      }
    }
    if (targetKeyRange == null) {
      targetKeyRange = new KeyRange();
      targetKeyRange.setKey(facetName);
      rq.add(targetKeyRange);
    }
    targetKeyRange.setMin(facetValue);
    executeNewSearch();
  }

  @Override
  public void removeTimeFacetAndRefresh(String facetName) {
    List<KeyRange> rq = currentSearch.getRangeQuery();
    if (rq != null) {
      List<KeyRange> newRq = new ArrayList<KeyRange>();
      for (KeyRange kv : rq) {
        if (!kv.getKey().equals(facetName)) {
          newRq.add(kv);
        }
      }
      currentSearch.setRangeQuery(newRq);
    }

    executeNewSearch();
  }

  @Override
  public String getDisplayForTimeFacet(String facetName, String facetValue) {
    return timeValueToDisplay.get(createTimeValueKey(facetName, facetValue));
  }

  @Override
  public void removeFacet(String facetName, String facetValue) {
    List<KeyValue> bq = currentSearch.getBooleanQuery();
    // check for existing facet and remove it
    for (KeyValue kv : bq) {
      if (kv.getKey().equals(facetName) && kv.getValue().equals(facetValue)) {
        bq.remove(kv);
        break;
      }
    }

    // set to first page
    executeNewSearch();
  }

  @Override
  public void clearSearch() {
    currentSearch = getBaseSearchQuery();
    executeNewSearch();
  }

  @Override
  public List<KeyValue> getAppliedFacets() {
    List<KeyValue> bq = currentSearch.getBooleanQuery();
    if (bq == null) {
      return new ArrayList<KeyValue>();
    } else {
      return bq;
    }
  }

  @Override
  public List<KeyRange> getAppliedTimeFacets() {
    List<KeyRange> bq = currentSearch.getRangeQuery();
    if (bq == null) {
      return new ArrayList<KeyRange>();
    } else {
      return bq;
    }
  }

  @Override
  public List<String> getFacetDisplayOrder() {
    return SearchQueryUtils.FACETS_DISPLAY_ORDER;
  }

  @Override
  public Long getStart() {
    return currentSearch.getStart();
  }

  @Override
  public Date getSearchStartTime() {
    if (searchStartTime == null) searchStartTime = new Date();
    return searchStartTime;
  }

  @Override
  public EntityType getEntityTypeForHit(Hit hit) {
    if (hit == null) return null;
    EntityType type = EntityType.valueOf(hit.getNode_type());
    return type;
  }

  @Override
  public String getCurrentSearchJSON() {
    String searchJSON = "";
    JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
    try {
      currentSearch.writeToJSONObject(adapter);
      searchJSON = adapter.toJSONString();
    } catch (JSONObjectAdapterException e) {
      view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
    }
    return searchJSON;
  }

  private SearchQuery checkForJson(String queryString) {
    SearchQuery query = getBaseSearchQuery();

    query.setQueryTerm(
      Arrays.asList(queryString.replace("%20", " ").split("\\s+"))
    );

    // if query parses into SearchQuery, use that, otherwise use it as a
    // search Term
    if (queryString != null) {
      String fixedQueryString = queryString;
      // check for url encoded
      if (queryString.startsWith("%7B")) {
        fixedQueryString = URL.decodePathSegment(queryString);
      }
      if (fixedQueryString.startsWith("{")) {
        try {
          query =
            new SearchQuery(jsonObjectAdapter.createNew(fixedQueryString));
          // passed a searchQuery
        } catch (JSONObjectAdapterException e) {
          // fall through to a use as search term
        }
      }
    }

    return query;
  }

  private SearchQuery getBaseSearchQuery() {
    SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
    timeValueToDisplay.clear();
    searchStartTime = new Date();
    return query;
  }

  private void executeSearch() {
    // Make sure url reflects current search. SWC-5181: the browser sometimes re-encodes the encoded search json
    Search searchPlace = new Search(getCurrentSearchJSON());
    globalApplicationState.replaceCurrentPlace(searchPlace);

    synAlert.clear();
    // Is there a search defined? If not, display empty result.
    if (isEmptyQuery()) {
      currentResult = new SearchResults();
      currentResult.setFound(new Long(0));
      view.setSearchResults(currentResult, "");
      loadMoreWidgetContainer.setIsMore(false);
      return;
    }
    AsyncCallback<SearchResults> callback = new AsyncCallback<SearchResults>() {
      @Override
      public void onSuccess(SearchResults result) {
        currentResult = result;
        String searchTerm = join(currentSearch.getQueryTerm(), " ");
        boolean isFirstPage =
          currentSearch.getStart() == null || currentSearch.getStart() == 0L;
        if (isFirstPage) {
          view.setSearchResults(currentResult, searchTerm);
        }
        Long limit = getLimit();
        currentSearch.setStart(currentResult.getStart() + limit);
        allPagesOfResults.add(currentResult);
        loadMoreWidgetContainer.add(
          view.getResults(currentResult, searchTerm, isFirstPage)
        );
        List<Hit> hits = currentResult.getHits();
        boolean isMore = limit.equals(new Long(hits.size()));
        loadMoreWidgetContainer.setIsMore(isMore);

        // Send search analytics data for results
        SearchResultPageReturnedEventData eventData =
          addSearchPageResultToAnalyticsEventData(
            new SearchResultPageReturnedEventData()
          );
        eventData.total_results = Double.valueOf(currentResult.getFound());
        searchAnalyticsClient.sendSearchResultPageReturnedEvent(eventData);
        hits.forEach(hit -> {
          SearchResultEventData resultEventData = getSearchResultEventData(hit);
          searchAnalyticsClient.sendSearchResultReturnedEvent(resultEventData);
        });
      }

      @Override
      public void onFailure(Throwable caught) {
        view.clear();
        loadMoreWidgetContainer.setIsMore(false);
        if (
          caught instanceof UnknownErrorException &&
          caught
            .getMessage()
            .contains(
              "AmazonCloudSearchDomain; Status Code: 500; Error Code: SearchException"
            )
        ) {
          synAlert.showError(
            "The search service is temporarily unavailable. We are currently working hard to fix this issue and we apologize for this inconvenience."
          );
        } else {
          synAlert.handleException(caught);
        }
      }
    };
    loadMoreWidgetContainer.setIsProcessing(true);
    jsClient.getSearchResults(currentSearch, callback);

    // Submit analytics event for search query submission
    searchAnalyticsClient.sendSearchQuerySubmittedEvent(
      addSearchRequestToAnalyticsEventData(new SearchQueryEventData())
    );
  }

  private boolean isEmptyQuery() {
    return (
      (currentSearch.getQueryTerm() == null ||
        currentSearch.getQueryTerm().size() == 0 ||
        (currentSearch.getQueryTerm().size() == 1 &&
          "".equals(currentSearch.getQueryTerm().get(0)))) &&
      (currentSearch.getBooleanQuery() == null ||
        currentSearch.getBooleanQuery().size() == 0) &&
      (currentSearch.getRangeQuery() == null ||
        currentSearch.getRangeQuery().size() == 0)
    );
  }

  private static String join(List<String> list, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for (String item : list) {
      sb.append(item);
      sb.append(delimiter);
    }
    String str = sb.toString();
    if (str.length() > 0) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }

  private String createTimeValueKey(String facetName, String facetValue) {
    return facetName + facetValue;
  }

  private long getLimit() {
    return currentSearch.getSize() == null ? 10L : currentSearch.getSize();
  }

  private int getIndexOfHit(Hit targetHit) {
    int cumulativeIndex = 0; // Note: 0-indexed
    for (SearchResults results : allPagesOfResults) {
      List<Hit> hits = results.getHits();
      int indexInPage = hits.indexOf(targetHit);
      if (indexInPage != -1) {
        return cumulativeIndex + indexInPage;
      }
      cumulativeIndex += hits.size();
    }
    return -1; // Hit not found
  }

  private int getIndexOfPageContainingHit(Hit targetHit) {
    // Note: 0-indexed
    for (int pageIndex = 0; pageIndex < allPagesOfResults.size(); pageIndex++) {
      List<Hit> hits = allPagesOfResults.get(pageIndex).getHits();
      if (hits.contains(targetHit)) {
        return pageIndex;
      }
    }
    return -1; // Hit not found
  }

  /**
   * Get event data for an individual search result
   * @param hit
   * @return
   */
  private SearchResultEventData getSearchResultEventData(Hit hit) {
    SearchResultEventData resultEventData =
      addSearchPageResultToAnalyticsEventData(new SearchResultEventData());
    resultEventData.page_index =
      Double.valueOf(1 + getIndexOfPageContainingHit(hit));
    resultEventData.rank = Double.valueOf(1 + getIndexOfHit(hit));
    resultEventData.item_type = SearchItemType.entity.toString();
    resultEventData.item_id = hit.getId();
    return resultEventData;
  }

  @Override
  public ClickHandler getSearchResultClickedHandler(Hit hit) {
    return event -> {
      SearchResultEventData resultEventData = getSearchResultEventData(hit);
      searchAnalyticsClient.sendSearchResultClickedEvent(resultEventData);
    };
  }

  /**
   * Adds data related to the latest search request that is not dependent on page
   * @param eventData
   */
  private <T extends SearchQueryEventData> T addSearchToAnalyticsEventData(
    T eventData
  ) {
    eventData.opensearch_enabled = false;
    eventData.search_context = SearchContext.synapse_entity.toString();

    JSONObjectAdapter adapter = this.jsonObjectAdapter.createNew();
    try {
      JSONArrayAdapter queryTermJSON = currentSearch
        .writeToJSONObject(adapter)
        .getJSONArray("queryTerm");
      if (queryTermJSON != null) {
        eventData.query_term = queryTermJSON.toJSONString();
      }
    } catch (JSONObjectAdapterException e) {
      jsniUtils.consoleError(
        "Error serializing queryTerm. It will be omitted from the analytics event."
      );
      jsniUtils.consoleError(e);
    }

    if (
      currentSearch.getBooleanQuery() != null &&
      !currentSearch.getBooleanQuery().isEmpty()
    ) {
      try {
        JSONArrayAdapter booleanQueryJSON = currentSearch
          .writeToJSONObject(adapter)
          .getJSONArray("booleanQuery");
        if (booleanQueryJSON != null) {
          eventData.serialized_boolean_query = booleanQueryJSON.toJSONString();
        }
      } catch (JSONObjectAdapterException e) {
        jsniUtils.consoleError(
          "Error serializing boolean query. It will be omitted from the analytics event."
        );
        jsniUtils.consoleError(e);
      }
    }
    if (
      currentSearch.getRangeQuery() != null &&
      !currentSearch.getRangeQuery().isEmpty()
    ) {
      try {
        JSONArrayAdapter rangeQueryJSON = currentSearch
          .writeToJSONObject(adapter)
          .getJSONArray("rangeQuery");
        if (rangeQueryJSON != null) {
          eventData.serialized_range_query = rangeQueryJSON.toJSONString();
        }
      } catch (JSONObjectAdapterException e) {
        jsniUtils.consoleError(
          "Error serializing range query. It will be omitted from the analytics event."
        );
        jsniUtils.consoleError(e);
      }
    }
    return eventData;
  }

  /**
   * Adds data related to the latest search request
   * @param eventData
   */
  private <
    T extends SearchQueryEventData
  > T addSearchRequestToAnalyticsEventData(T eventData) {
    eventData = addSearchToAnalyticsEventData(eventData);
    eventData.start_index =
      (double) (currentSearch.getStart() == null
          ? 0
          : currentSearch.getStart());
    eventData.page_index =
      (double) (currentSearch.getStart() == null
          ? 1
          : 1 + (currentSearch.getStart() / getLimit()));
    return eventData;
  }

  /**
   * Adds data related to the latest search result
   * @param eventData
   */
  private <
    T extends SearchQueryEventData
  > T addSearchPageResultToAnalyticsEventData(T eventData) {
    eventData = addSearchToAnalyticsEventData(eventData);

    eventData.start_index = Double.valueOf(currentResult.getStart());
    eventData.page_index =
      Double.valueOf(1 + (currentResult.getStart() / getLimit()));

    return eventData;
  }
}
