package org.sagebionetworks.web.client.jsinterop;

import elemental2.core.JsObject;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SxProps extends JsObject {

  public String color;

  public long fontWeight;

  public String mt;

  public String pl;
  public String pb;

  public String borderColor;

  @JsOverlay
  public static SxProps create() {
    return new SxProps();
  }

  @JsOverlay
  public final SxProps setBorderColor(String borderColor) {
    this.borderColor = borderColor;
    return this;
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
