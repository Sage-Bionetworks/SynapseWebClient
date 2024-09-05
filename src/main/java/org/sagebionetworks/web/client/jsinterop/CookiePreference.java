package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CookiePreference extends ReactComponentProps {

  @JsNullable
  public boolean functionalAllowed;

  @JsNullable
  public boolean analyticsAllowed;

  @JsOverlay
  public static CookiePreference create(
    boolean functionalAllowed,
    boolean analyticsAllowed
  ) {
    CookiePreference options = new CookiePreference();
    options.functionalAllowed = functionalAllowed;
    options.analyticsAllowed = analyticsAllowed;
    return options;
  }
}
