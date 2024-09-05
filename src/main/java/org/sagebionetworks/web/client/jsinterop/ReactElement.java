package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ReactElement<
  T extends ReactComponentType<P>, P extends ReactComponentProps
> {

  public T type;

  @JsNullable
  public P props;

  @JsNullable
  public String key;
}
