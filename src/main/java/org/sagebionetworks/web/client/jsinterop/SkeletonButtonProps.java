package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SkeletonButtonProps extends ReactComponentProps {

  @JsNullable
  String placeholderText;

  @JsOverlay
  public static SkeletonButtonProps create(String placeholderText) {
    SkeletonButtonProps props = new SkeletonButtonProps();
    props.placeholderText = placeholderText;
    return props;
  }
}
