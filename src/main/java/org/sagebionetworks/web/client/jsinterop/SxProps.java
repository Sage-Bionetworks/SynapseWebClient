package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SxProps {

  private String color;
  private long fontWeight;

  @JsOverlay
  public static SxProps create() {
    return new SxProps();
  }

  @JsOverlay
  public final SxProps setColor(String color) {
    this.color = color;
    return this;
  }

  @JsOverlay
  public final SxProps setFontWeight(long fontWeight) {
    this.fontWeight = fontWeight;
    return this;
  }
}
