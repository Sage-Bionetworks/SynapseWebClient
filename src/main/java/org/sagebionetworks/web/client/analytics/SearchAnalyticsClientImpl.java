package org.sagebionetworks.web.client.analytics;

import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchQueryEventData;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchResultEventData;
import org.sagebionetworks.web.client.jsinterop.analytics.SearchResultPageReturnedEventData;

public class SearchAnalyticsClientImpl implements SearchAnalyticsClient {

  @Inject
  public SearchAnalyticsClientImpl() {}

  @Override
  public void sendSearchQuerySubmittedEvent(SearchQueryEventData eventData) {
    SRC.Analytics.sendSearchQuerySubmittedEvent(eventData);
  }

  @Override
  public void sendSearchResultPageReturnedEvent(
    SearchResultPageReturnedEventData eventData
  ) {
    SRC.Analytics.sendSearchResultsReturnedEvent(eventData);
  }

  @Override
  public void sendSearchResultReturnedEvent(SearchResultEventData eventData) {
    SRC.Analytics.sendSearchResultReturnedEvent(eventData);
  }

  @Override
  public void sendSearchResultClickedEvent(SearchResultEventData eventData) {
    SRC.Analytics.sendSearchResultClickedEvent(eventData);
  }
}
