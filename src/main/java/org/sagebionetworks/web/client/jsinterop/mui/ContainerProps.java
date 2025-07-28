package org.sagebionetworks.web.client.jsinterop.mui;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ContainerProps extends PropsWithSx {

  @JsProperty
  public String maxWidth; // Options: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | false

  @JsProperty
  public boolean disableGutters;

  @JsProperty
  public boolean fixed;

  @JsProperty
  public String children;

  @JsOverlay
  public static ContainerProps create() {
    return new ContainerProps();
  }
}
