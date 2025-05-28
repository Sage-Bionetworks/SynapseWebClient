package org.sagebionetworks.web.client.jsinterop.mui;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.PropsWithStyle;
import org.sagebionetworks.web.client.jsinterop.SxProps;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class PropsWithSx extends PropsWithStyle {

  @JsNullable
  SxProps sx;

  @JsOverlay
  public static PropsWithSx create() {
    return new PropsWithSx();
  }
}
