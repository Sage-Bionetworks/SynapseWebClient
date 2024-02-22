package org.sagebionetworks.web.client.jsinterop.reactquery;

import java.util.List;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "ReactQuery")
public class QueryClient {

  public QueryClient(QueryClientOptions config) {}

  /**
   * Removes all cached query data and triggers a refetch.
   */
  public native void resetQueries();

  /**
   * Triggers a refetch of matching query data without removing the cached data.
   * @param filters
   */
  public native void invalidateQueries(InvalidateQueryFilters filters);
}
