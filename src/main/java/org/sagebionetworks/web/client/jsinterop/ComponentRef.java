package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JsInterop class that represents React component ref.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ComponentRef {

  public Object current;
}
