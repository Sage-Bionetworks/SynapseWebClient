package org.sagebionetworks.web.client.jsinterop.reactquery;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class QueryClientOptions {

  public DefaultQueryClientOptions defaultOptions;

  @JsOverlay
  public static QueryClientOptions create() {
    QueryClientOptions qco = new QueryClientOptions();
    qco.defaultOptions = DefaultQueryClientOptions.create();
    return qco;
  }
}
