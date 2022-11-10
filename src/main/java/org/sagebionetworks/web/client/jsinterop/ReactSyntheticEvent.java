package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ReactSyntheticEvent<E, C, T> {

  public E nativeEvent;
  public C currentTarget;
  public T target;
  public boolean bubbles;
  public boolean cancelable;
  public boolean defaultPrevented;
  public long eventPhase;
  public boolean isTrusted;

  public native void preventDefault();

  public native boolean isDefaultPrevented();

  public native void stopPropagation();

  public native boolean isPropagationStopped();

  public native void persist();

  public long timeStamp;
  public String type;
}
