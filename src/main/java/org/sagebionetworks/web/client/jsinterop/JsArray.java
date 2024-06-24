package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JsInterop class that represents the JavaScript Array builtin.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Array")
public class JsArray<T> {

  @SafeVarargs
  @JsConstructor
  public JsArray(T... items) {}

  public native void push(T item);
}
