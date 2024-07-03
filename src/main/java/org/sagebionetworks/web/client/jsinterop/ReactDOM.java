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

  /**
   * flushSync lets you force React to flush any updates inside the provided callback synchronously. This ensures that the DOM is updated immediately.
   * Using flushSync is uncommon and can hurt the performance of your app. Most of the time, flushSync can be avoided. Use flushSync as last resort.
   * <a href="https://react.dev/reference/react-dom/flushSync">See docs</a>
   */
  public static native void flushSync();

  /**
   * flushSync lets you force React to flush any updates inside the provided callback synchronously. This ensures that the DOM is updated immediately.
   * Using flushSync is uncommon and can hurt the performance of your app. Most of the time, flushSync can be avoided. Use flushSync as last resort.
   * <a href="https://react.dev/reference/react-dom/flushSync">See docs</a>
   */
  public static native void flushSync(Callback callback);
}
