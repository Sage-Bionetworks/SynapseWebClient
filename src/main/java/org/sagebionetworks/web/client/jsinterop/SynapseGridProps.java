package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseGridProps extends ReactComponentProps {

  public boolean showDebugInfo;
  public Object ref; // ref may be a RefObject or a RefCallback, but we can't express that in Java!

  @JsOverlay
  private static SynapseGridProps create(Boolean showDebugInfo, Object ref) {
    SynapseGridProps props = new SynapseGridProps();
    props.showDebugInfo = showDebugInfo;
    props.ref = ref;
    return props;
  }

  @JsOverlay
  public static SynapseGridProps create(
    Boolean showDebugInfo,
    ReactRefCallback<SynapseGridHandle> ref
  ) {
    return create(showDebugInfo, (Object) ref);
  }

  @JsOverlay
  public static SynapseGridProps create(
    Boolean showDebugInfo,
    ReactRefObject<SynapseGridHandle> ref
  ) {
    return create(showDebugInfo, (Object) ref);
  }
}
