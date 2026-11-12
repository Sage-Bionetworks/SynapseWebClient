package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * The session an access token belongs to, passed to SRC's KeyFactory so anonymous sessions use the same query keys
 * that SRC's SynapseContextProvider uses.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class KeyFactorySession {

  public boolean isAuthenticated;

  @JsNullable
  public String realmId;

  @JsOverlay
  public static KeyFactorySession create(
    boolean isAuthenticated,
    String realmId
  ) {
    KeyFactorySession session = new KeyFactorySession();
    session.isAuthenticated = isAuthenticated;
    session.realmId = realmId;
    return session;
  }
}
