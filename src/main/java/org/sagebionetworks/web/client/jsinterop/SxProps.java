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

  public String backgroundColor;

  public String fontSize;

  public String fontStyle;

  public String lineHeight;

  public String letterSpacing;

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
  public final SxProps setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
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

  @JsOverlay
  public final SxProps setFontSize(String fontSize) {
    this.fontSize = fontSize;
    return this;
  }

  @JsOverlay
  public final SxProps setFontStyle(String fontStyle) {
    this.fontStyle = fontStyle;
    return this;
  }

  @JsOverlay
  public final SxProps setLineHeight(String lineHeight) {
    this.lineHeight = lineHeight;
    return this;
  }

  @JsOverlay
  public final SxProps setLetterSpacing(String letterSpacing) {
    this.letterSpacing = letterSpacing;
    return this;
  }
}
