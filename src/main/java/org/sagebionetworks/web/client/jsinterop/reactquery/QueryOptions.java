package org.sagebionetworks.web.client.jsinterop.reactquery;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class QueryOptions {

  public long staleTime;
  public long cacheTime;
  public boolean retry;
  public boolean refetchOnWindowFocus;

  @JsOverlay
  public static QueryOptions create() {
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.staleTime = 60 * 1000L; // 60s
    queryOptions.cacheTime = 1000L * 60 * 30; // 30 min
    queryOptions.retry = false; // SynapseClient knows which queries to retry
    queryOptions.refetchOnWindowFocus = false;
    return queryOptions;
  }
}
