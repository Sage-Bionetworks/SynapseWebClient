package org.sagebionetworks.web.client.analytics;

import org.sagebionetworks.web.client.jsinterop.analytics.SearchQueryEventData;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchResultEventData;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchResultPageReturnedEventData;

public interface SearchAnalyticsClient {
  /**
   * Submits an event to Google Analytics corresponding to a submitted search query.
   *
   * @param eventData - The event data associated with the search query
   */
  void sendSearchQuerySubmittedEvent(SearchQueryEventData eventData);

  /**
   * Submits an event to Google Analytics corresponding to a returned page of search results.
   *
   * @param eventData - The event data associated with the search results
   */
  void sendSearchResultPageReturnedEvent(
    SearchResultPageReturnedEventData eventData
  );

  /**
   * Submits an event to Google Analytics corresponding to an individual returned search result.
   *
   * @param eventData - The event data associated with the search result
   */
  void sendSearchResultReturnedEvent(SearchResultEventData eventData);

  /**
   * Submits an event to Google Analytics corresponding to a clicked search result.
   *
   * @param eventData - The event data associated with the search result
   */
  void sendSearchResultClickedEvent(SearchResultEventData eventData);
}
