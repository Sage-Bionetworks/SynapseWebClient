package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EmptyProps extends ReactComponentProps {

  @JsOverlay
  public static EmptyProps create() {
    EmptyProps props = new EmptyProps();
    return props;
  }
}
