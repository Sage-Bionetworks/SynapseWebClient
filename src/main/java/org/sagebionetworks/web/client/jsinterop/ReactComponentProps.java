package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ReactComponentProps {

  @JsConstructor
  public ReactComponentProps() {}

  @JsNullable
  public String key;

  // Either a ComponentRef or CallbackRef may be passed. A CallbackRef will be invoked when the ref is set.
  @JsNullable
  public Object ref;
}
