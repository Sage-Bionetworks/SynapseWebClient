package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class JSON {

  public static native Object parse(String json);
}
