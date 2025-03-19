package org.sagebionetworks.web.client.jsinterop.analytics;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SearchResultPageReturnedEventData extends SearchQueryEventData {

  /** The total number of results yielded by this query, if provided by the server */
  @JsNullable
  public Double total_results;
}
