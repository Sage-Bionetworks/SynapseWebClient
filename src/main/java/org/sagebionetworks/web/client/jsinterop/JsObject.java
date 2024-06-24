package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JsInterop class that represents the JavaScript Object builtin.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class JsObject {

  @JsConstructor
  public JsObject() {}

  public static native JsObject assign(JsObject target, JsObject... sources);
}
