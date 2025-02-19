package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.dom.client.Element;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "ReactDOMClient")
public class ReactDOMClient {

  public static native ReactDOMRoot createRoot(Element container);
}
