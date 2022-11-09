package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class SynapseClientError {

  String status;
  String reason;
  String url;

  @JsOverlay
  public final String getReason() {
    return this.reason;
  }

  /**
   * Setter exposed for testing purposes only, because Mockito cannot mock a native object
   * @param reason
   */
  @JsOverlay
  public final void setReason(String reason) {
    this.reason = reason;
  }
}
