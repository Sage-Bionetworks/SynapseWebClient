package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public interface StringRecord {
  @JsOverlay
  default String get(String key) {
    return (String) Js.asPropertyMap(this).get(key);
  }

  @JsOverlay
  default void set(String key, String value) {
    Js.asPropertyMap(this).set(key, value);
  }
}
