package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JsInterop mirror of the TypeScript {@code SessionState} type from
 * {@code SynapseSessionManager}.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SessionStateJsObject {

  @JsNullable
  public String token;

  @JsNullable
  public String realmId;

  @JsNullable
  public String userId;

  public boolean isAuthenticated;
  public boolean hasInitializedSession;
}
