package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityBadgeIconsProps extends ReactComponentProps {

  @JsFunction
  public interface OnUnlinkSuccess {
    void onUnlinkSuccess(String entityId);
  }

  @JsFunction
  public interface OnUnlinkError {
    void onUnlinkError(SynapseClientError error);
  }

  String entityId;
  OnUnlinkSuccess onUnlink;
  OnUnlinkError onUnlinkError;
  boolean renderTooltipComponent;

  @JsOverlay
  public final OnUnlinkSuccess getOnUnlinkSuccess() {
    return this.onUnlink;
  }

  @JsOverlay
  public final OnUnlinkError getOnUnlinkError() {
    return this.onUnlinkError;
  }

  @JsOverlay
  public static EntityBadgeIconsProps create(
    String entityId,
    OnUnlinkSuccess onUnlink,
    OnUnlinkError onUnlinkError
  ) {
    EntityBadgeIconsProps props = new EntityBadgeIconsProps();
    props.entityId = entityId;
    props.onUnlink = onUnlink;
    props.onUnlinkError = onUnlinkError;
    props.renderTooltipComponent = true;
    return props;
  }
}
