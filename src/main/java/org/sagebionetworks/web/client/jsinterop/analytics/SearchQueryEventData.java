package org.sagebionetworks.web.client.jsinterop.analytics;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SearchQueryEventData {

  /** The search term that the user entered */
  public String query_term;
  /** The context in which the user performed the search */
  public String search_context;

  /** Serialized boolean filter(s) that may have been applied along with the query_term */
  @JsNullable
  public String serialized_boolean_query;

  /** Serialized range filter(s) that may have been applied along with the query_term */
  @JsNullable
  public String serialized_range_query;

  /** The offset of the first result of the page. 0-indexed */
  public Double start_index;

  /** The page number of the displayed search results. 1-indexed */
  public Double page_index;

  /** True if the search was performed using the AWS OpenSearch backend */
  public boolean opensearch_enabled;
}
