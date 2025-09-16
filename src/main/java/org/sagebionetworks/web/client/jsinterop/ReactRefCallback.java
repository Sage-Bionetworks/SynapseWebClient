package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;

@JsFunction
public interface ReactRefCallback<T> {
  void run(T node);
}
