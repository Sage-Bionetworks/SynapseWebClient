package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;

@JsFunction
public interface ReactMouseEventHandler {
  void onClick(ReactMouseEvent event);
}
