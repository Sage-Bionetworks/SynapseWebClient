package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

/**
 * React prop type that includes a `style` prop containing CSS properties.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class PropsWithStyle extends ReactComponentProps {

  public JsPropertyMap<String> style;
}
