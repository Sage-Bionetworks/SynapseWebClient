package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.dom.client.Element;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ReactDOM {

  @JsFunction
  @FunctionalInterface
  public interface Callback {
    void run();
  }

  public static native ReactDOMRoot createRoot(Element container);

  public static native boolean unmountComponentAtNode(Element container);
}
