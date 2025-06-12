package org.sagebionetworks.web.client.jsinterop.util;

import jsinterop.annotations.JsFunction;

/**
 * JsInterop-compatible version of {@link java.lang.Runnable}.
 */
@FunctionalInterface
@JsFunction
public interface JsRunnable {
  void run();
}
