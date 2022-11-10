package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class IconSvgProps extends ReactComponentProps {

  String icon;

  @JsNullable
  String label;

  @JsOverlay
  public static IconSvgProps create(String icon, String label) {
    IconSvgProps props = new IconSvgProps();
    props.icon = icon;
    props.label = label;
    return props;
  }
}
