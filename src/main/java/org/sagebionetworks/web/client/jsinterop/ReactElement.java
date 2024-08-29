package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ReactElement<T extends ReactComponentProps> {

  @JsNullable
  public T props;

  @JsNullable
  public ComponentRef ref;
}
