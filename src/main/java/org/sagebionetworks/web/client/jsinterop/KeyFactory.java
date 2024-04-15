package org.sagebionetworks.web.client.jsinterop;

import java.util.List;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryKey;

@JsType(isNative = true, namespace = "SRC.SynapseQueries")
public class KeyFactory {

  @JsConstructor
  public KeyFactory(String accessToken) {}

  public native QueryKey getDownloadListBaseQueryKey();

  public native QueryKey getEntityQueryKey(String entityId);

  public native QueryKey getTrashCanItemsQueryKey();
}
