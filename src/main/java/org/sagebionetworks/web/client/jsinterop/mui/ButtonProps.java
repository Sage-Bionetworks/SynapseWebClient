package org.sagebionetworks.web.client.jsinterop.mui;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ButtonProps extends PropsWithSx {

  @JsNullable
  String id;

  @JsProperty
  public String variant;

  @JsProperty
  public String color;

  @JsProperty
  public String size;

  @JsProperty
  public Object startIcon;

  @JsProperty
  public String href;

  @JsProperty
  public boolean fullWidth;

  @JsProperty
  public boolean disabled;

  @JsProperty
  public String target;

  @JsProperty
  public String children;

  @JsOverlay
  public static ButtonProps create() {
    return new ButtonProps();
  }
}
