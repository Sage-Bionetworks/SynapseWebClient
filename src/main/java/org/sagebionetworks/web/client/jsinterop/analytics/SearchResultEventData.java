package org.sagebionetworks.web.client.jsinterop.analytics;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SearchResultEventData extends SearchQueryEventData {

  /** The type of the item that was returned */
  public String item_type;
  /** The ID of the item that was returned */
  public String item_id;
  /** The rank of the item that was returned in those search results */
  public Double rank;
  /** The offset of the item that was returned in the full set of search results. 0-indexed */
  public Double page_index;
}
