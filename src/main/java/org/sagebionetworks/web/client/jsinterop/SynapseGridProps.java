package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseGridProps extends ReactComponentProps {

  public String query;
  public boolean showDebugInfo;

  @JsOverlay
  public static SynapseGridProps create(String query, Boolean showDebugInfo) {
    SynapseGridProps props = new SynapseGridProps();
    props.query = query;
    props.showDebugInfo = showDebugInfo;
    return props;
  }
}
