package org.sagebionetworks.web.client.jsinterop.util;

import jsinterop.annotations.JsFunction;

/**
 * JsInterop-compatible version of {@link java.util.function.Supplier}.
 */
@FunctionalInterface
@JsFunction
public interface JsSupplier<T> {
  T get();
}
