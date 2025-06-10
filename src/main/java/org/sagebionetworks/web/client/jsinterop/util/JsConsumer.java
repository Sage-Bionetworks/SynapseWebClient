package org.sagebionetworks.web.client.jsinterop.util;

import jsinterop.annotations.JsFunction;

/**
 * JsInterop-compatible version of {@link java.util.function.Consumer}.
 */
@FunctionalInterface
@JsFunction
public interface JsConsumer<T> {
  void accept(T t);
}
