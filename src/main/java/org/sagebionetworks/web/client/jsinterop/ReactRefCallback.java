package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;

/**
 * A React Ref callback, which is invoked with the referenced node when it changes.
 *
 * While this functional interface represents a ReactRef, this Java interface does not extend the {@link ReactRef}
 * interface because of limitations in JsInterop.
 *
 * For more information about ref callbacks, see: https://react.dev/reference/react-dom/components/common#ref-callback
 * @param <T>
 */
@JsFunction
public interface ReactRefCallback<T> {
  void run(T node);
}
