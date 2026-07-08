package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntitySidebarProps extends ReactComponentProps {

  String entityId;
  Double versionNumber;

  @JsOverlay
  public static EntitySidebarProps create(
    String entityId,
    Double versionNumber
  ) {
    EntitySidebarProps props = new EntitySidebarProps();
    props.entityId = entityId;
    props.versionNumber = versionNumber;
    return props;
  }
}
