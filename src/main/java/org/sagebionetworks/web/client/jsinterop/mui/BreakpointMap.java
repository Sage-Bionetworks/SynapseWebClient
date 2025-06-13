package org.sagebionetworks.web.client.jsinterop.mui;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.PropsWithStyle;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class BreakpointMap {

  @JsNullable
  int xs;

  @JsNullable
  int sm;

  @JsNullable
  int md;

  @JsNullable
  int lg;

  @JsNullable
  int xl;

  @JsOverlay
  public static BreakpointMap create() {
    return new BreakpointMap();
  }
}
