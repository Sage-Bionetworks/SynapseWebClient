package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ReactPointerEvent extends ReactMouseEvent {

  public long pointerId;
  public double pressure;
  public double tangentialPressure;
  public double tiltX;
  public double tiltY;
  public double twist;
  public long width;
  public long height;
  public String pointerType;
  public boolean isPrimary;
}
