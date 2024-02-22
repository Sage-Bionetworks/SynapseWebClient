package org.sagebionetworks.web.client.jsinterop.reactquery;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class InvalidateQueryFilters {

  Object[] queryKey;

  @JsOverlay
  public static InvalidateQueryFilters create(Object[] queryKey) {
    InvalidateQueryFilters props = new InvalidateQueryFilters();
    props.queryKey = queryKey;
    return props;
  }
}
