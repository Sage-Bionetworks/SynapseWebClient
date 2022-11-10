package org.sagebionetworks.web.client.jsinterop.reactquery;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DefaultQueryClientOptions {

  public QueryOptions queries;

  @JsOverlay
  public static DefaultQueryClientOptions create() {
    DefaultQueryClientOptions defaultQueryClientOptions = new DefaultQueryClientOptions();
    defaultQueryClientOptions.queries = QueryOptions.create();
    return defaultQueryClientOptions;
  }
}
