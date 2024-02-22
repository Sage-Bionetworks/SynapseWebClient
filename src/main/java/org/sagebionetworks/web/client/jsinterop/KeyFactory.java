package org.sagebionetworks.web.client.jsinterop;

import java.util.List;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "SRC.SynapseQueries")
public class KeyFactory {

  @JsConstructor
  public KeyFactory(String accessToken) {}

  public native Object[] getDownloadListBaseQueryKey();

  public native Object[] getEntityQueryKey(String entityId);
}
