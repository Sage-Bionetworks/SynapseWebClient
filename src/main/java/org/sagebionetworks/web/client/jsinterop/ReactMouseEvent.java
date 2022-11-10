package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ReactMouseEvent extends ReactSyntheticEvent {

  public boolean altKey;
  public long button;
  public long buttons;
  public double clientX;
  public double clientY;
  public boolean ctrlKey;

  public native boolean getModifierState(String key);

  public boolean metaKey;
  public long movementX;
  public long movementY;
  public long pageX;
  public long pageY;

  @JsNullable
  public Object relatedTarget;

  public long screenX;
  public long screenY;
  public boolean shiftKey;
}
